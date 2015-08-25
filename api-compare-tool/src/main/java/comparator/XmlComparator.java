package comparator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * A custom xml comparator deals xmls like json, nodes can only have text or sub
 * nodes, nodes with type="array" are ordered and can have sub nodes with
 * duplicated name, the others are disordered and can't have sub nodes with
 * duplicated name. Nodes which aren't text node are all expanded as lines, text
 * node will be displayed in one line. Text nodes with same name and different
 * value will be displayed twice. Add a difference mark at beginning of each
 * line.
 * 
 * @author ruochen.xu
 *
 */
public class XmlComparator {
	private static int TAB_LEN = 4;

	/**
	 * Generate spaces.
	 * 
	 * @param deep
	 *            Deep in xml.
	 * @return TAB_LEN * deep spaces.
	 */
	private static String spaces(int deep) {
		if (deep == 0) {
			return "";
		} else {
			return String.format("%" + TAB_LEN * deep + "s", "");
		}
	}

	/**
	 * Get node head as string.
	 * 
	 * @param node
	 * @return
	 */
	private static String printNodeHead(Element node) {
		StringBuilder now = new StringBuilder();
		now.append("<").append(node.getName());
		for (Object o : node.attributes()) {
			Attribute a = (Attribute) o;
			now.append(" ").append(a.getName()).append("=\"")
					.append(a.getValue()).append("\"");
		}
		now.append(">");
		return now.toString();
	}

	/**
	 * Print xml node in custom format.
	 * 
	 * @param node
	 * @param deep
	 *            deep in xml.
	 * @param prefix
	 *            Marker of the node.
	 * @return Formatted output.
	 * @throws DocumentException
	 */
	private static StringBuilder formatPrint(Element node, int deep,
			String prefix) throws DocumentException {
		if (node.hasMixedContent()) {
			// Can't handle mixed content.
			throw new DocumentException("Has mixed content: " + node.asXML());
		}
		StringBuilder now = new StringBuilder();
		now.append(prefix).append(spaces(deep)).append(printNodeHead(node));
		if (node.hasContent()) {
			if (node.isTextOnly()) {
				// Display text node in one line.
				now.append(node.getText());
			} else {
				now.append("\n");
				for (Object o : node.elements()) {
					now.append(formatPrint((Element) o, deep + 1, prefix));
				}
				now.append(prefix).append(spaces(deep));
			}
			now.append("</").append(node.getName()).append(">");
		} else {
			now.insert(now.length() - 1, '/');
		}
		now.append("\n");
		return now;
	}

	/**
	 * Check whether a node is an array.
	 * 
	 * @param node
	 * @return
	 * @throws DocumentException 
	 */
	private static boolean isArray(Element node) throws DocumentException {
		Attribute type = node.attribute("type");
		if ((type == null) || (!type.getValue().equals("array"))) {
			return false;
		}
		if ((node.hasContent()) && (node.isTextOnly())) {
			// Can't handle text in array.
			throw new DocumentException("Text in array: " + node.asXML());
		}
		return true;
	}

	/**
	 * Get a Map<head, node> of sub nodes of specified node.
	 * 
	 * @param node
	 * @return
	 * @throws DocumentException
	 */
	private static Map<String, Element> getSubnodeMap(Element node)
			throws DocumentException {
		Map<String, Element> map = new HashMap<String, Element>();
		for (Object o : node.elements()) {
			Element e = (Element) o;
			String head = printNodeHead(e);
			if (map.containsKey(head)) {
				// Can't handle duplicated sub node.
				throw new DocumentException("Has duplicated head: "
						+ node.asXML());
			} else {
				map.put(printNodeHead(e), e);
			}
		}
		return map;
	}

	/**
	 * Compare two xml node and print them in custom format.
	 * 
	 * @param a
	 * @param b
	 * @param deep
	 *            deep in json.
	 * @return Formatted output.
	 * @throws DocumentException
	 */
	private static StringBuilder compareNode(Element a, Element b, int deep)
			throws DocumentException {
		// Can't handle mixed content.
		if (a.hasMixedContent()) {
			throw new DocumentException("Has mixed content: " + a.asXML());
		}
		if (b.hasMixedContent()) {
			throw new DocumentException("Has mixed content: " + b.asXML());
		}
		StringBuilder now = new StringBuilder();
		List<StringBuilder> subs = null;
		char prefix = ' ';
		if (!printNodeHead(a).equals(printNodeHead(b))) {
			// Different node head.
			now.append(formatPrint(a, deep, "+"));
			now.append(formatPrint(b, deep, "-"));
		} else if ((a.isTextOnly()) && (b.isTextOnly())
				&& (a.getText().equals(b.getText()))) {
			// Same text node.
			now.append(formatPrint(a, deep, " "));
		} else if (isArray(a) && isArray(b)) {
			// Both array.
			subs = new ArrayList<StringBuilder>();
			int i = 0;
			while ((i < a.elements().size()) && (i < b.elements().size())) {
				subs.add(compareNode((Element) a.elements().get(i), (Element) b
						.elements().get(i), deep + 1));
				i++;
			}
			while (i < a.elements().size()) {
				subs.add(formatPrint((Element) a.elements().get(i), deep + 1,
						"+"));
				i++;
			}
			while (i < b.elements().size()) {
				subs.add(formatPrint((Element) b.elements().get(i), deep + 1,
						"-"));
				i++;
			}
			// Check difference.
			for (StringBuilder sb : subs) {
				if (sb.charAt(0) != ' ') {
					prefix = '*';
				}
			}
			// Generate text.
			now.append(prefix).append(spaces(deep)).append(printNodeHead(a))
					.append("\n");
			now.append(StringUtils.join(subs, ""));
			now.append(prefix).append(spaces(deep)).append("</")
					.append(a.getName()).append(">").append("\n");
		} else if ((a.elements().size() > 0) && (b.elements().size() > 0)) {
			// Both object.
			Map<String, Element> am = getSubnodeMap(a);
			Map<String, Element> bm = getSubnodeMap(b);
			subs = new ArrayList<StringBuilder>();
			for (String k : am.keySet()) {
				if (bm.containsKey(k)) {
					// Compare common key.
					subs.add(compareNode(am.get(k), bm.get(k), deep + 1));
					bm.remove(k);
				} else {
					// Only a have.
					subs.add(formatPrint(am.get(k), deep + 1, "+"));
				}
			}
			for (String k : bm.keySet()) {
				// Only b have.
				subs.add(formatPrint(bm.get(k), deep + 1, "-"));
			}
			// Check difference.
			for (StringBuilder sb : subs) {
				if (sb.charAt(0) != ' ') {
					prefix = '*';
				}
			}
			// Generate text.
			now.append(prefix).append(spaces(deep)).append(printNodeHead(a))
					.append("\n");
			now.append(StringUtils.join(subs, ""));
			now.append(prefix).append(spaces(deep)).append("</")
					.append(a.getName()).append(">").append("\n");
		} else {
			// Different type of node or different text node.
			now.append(formatPrint(a, deep, "+"));
			now.append(formatPrint(b, deep, "-"));

		}
		return now;
	}

	/**
	 * Convert two string to xml, compare them and print the difference.
	 * 
	 * @param a
	 * @param b
	 * @return Formated output.
	 * @throws DocumentException
	 */

	public static String compare(String a, String b) throws DocumentException {
		Document da = null;
		Document db = null;
		try {
			da = DocumentHelper.parseText(a);
			db = DocumentHelper.parseText(b);
		} catch (DocumentException e) {
			return null;
		}
		return compareNode(da.getRootElement(), db.getRootElement(), 0)
				.toString();
	}

	/**
	 * Comapre two xml element.
	 *
	 * @param a
	 * @param b
	 * @return Formated output.
	 * @throws DocumentException
	 */
	public static String compare(Element a, Element b) throws DocumentException {
		return compareNode(a, b, 0).toString();
	}

	public static String readFile(String name) throws IOException {
		StringBuilder s = new StringBuilder();
		BufferedReader br = new BufferedReader(new FileReader(name));
		String now = null;
		while ((now = br.readLine()) != null) {
			s.append(now);
		}
		br.close();
		return s.toString();
	}
}
