package comparator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A json comparator, follow standard json rules, objects are disordered, arrays are
 * ordered. Objects and arrays are all expanded as lines, same keys with
 * different values will be displayed twice. Add a difference mark at beginning of
 * each line.
 * 
 * @author ruochen.xu
 *
 */
public class JsonComparator {
	private static int TAB_LEN = 4;

	/**
	 * Generate spaces.
	 * 
	 * @param deep
	 *            Deep in json.
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
	 * Get key list in json object.
	 * 
	 * @param json
	 * @return List of key string.
	 */
	private static List<String> getKeyList(JsonNode json) {
		List<String> list = new ArrayList<String>();
		Iterator<String> iter = json.fieldNames();
		while (iter.hasNext()) {
			list.add(iter.next());
		}
		return list;
	}

	/**
	 * Get json node list in json array.
	 * 
	 * @param json
	 * @return List of json node.
	 */
	private static List<JsonNode> getNodeList(JsonNode json) {
		List<JsonNode> list = new ArrayList<JsonNode>();
		Iterator<JsonNode> iter = json.elements();
		while (iter.hasNext()) {
			list.add(iter.next());
		}
		return list;
	}

	/**
	 * Print a json node in custom format.
	 * 
	 * @param node
	 * @param key
	 *            The key of the json node in json object, empty string for a
	 *            node in json array.
	 * @param deep
	 *            deep in json.
	 * @param prefix
	 *            Marker of the node.
	 * @return Formatted output.
	 */
	private static StringBuilder formatPrint(JsonNode node, String key,
			int deep, String prefix) {
		List<StringBuilder> subs = null;
		StringBuilder now = new StringBuilder();
		// Add object key.
		now.append(prefix).append(spaces(deep)).append(key);
		if (node.isObject()) {
			now.append("{\n");
			subs = new ArrayList<StringBuilder>();
			for (String name : getKeyList(node)) {
				subs.add(formatPrint(node.get(name), name + ": ", deep + 1,
						prefix));
			}
			now.append(StringUtils.join(subs, ",\n"));
			if (!subs.isEmpty()) {
				now.append("\n");
			}
			now.append(prefix).append(spaces(deep)).append("}");
		} else if (node.isArray()) {
			now.append("[\n");
			subs = new ArrayList<StringBuilder>();
			for (JsonNode j : getNodeList(node)) {
				subs.add(formatPrint(j, "", deep + 1, prefix));
			}
			now.append(StringUtils.join(subs, ",\n"));
			if (!subs.isEmpty()) {
				now.append("\n");
			}
			now.append(prefix).append(spaces(deep)).append("]");
		} else {
			now.append(node.toString());
		}
		return now;
	}

	/**
	 * Compare two json node and print them in custom format.
	 * 
	 * @param a
	 *            The first json node.
	 * @param b
	 *            The second json node.
	 * @param key
	 *            The key of the json node in json object, empty string for a
	 *            node in json array.
	 * @param deep
	 *            deep in json.
	 * @return Formatted output.
	 */
	private static StringBuilder compareNode(JsonNode a, JsonNode b,
			String key, int deep) {
		List<StringBuilder> subs = null;
		StringBuilder now = new StringBuilder();
		if (a.equals(b)) {
			// Normally print if equal.
			// HashMap implement this method in O(1).
			now = formatPrint(a, key, deep, " ");
		} else if ((a.isObject()) && (b.isObject())) {
			// If both object, compare them according the keys.
			subs = new ArrayList<StringBuilder>();
			List<String> al = getKeyList(a);
			List<String> bl = getKeyList(b);
			for (String k : al) {
				if (bl.contains(k)) {
					// Compare common key.
					subs.add(compareNode(a.get(k), b.get(k), k + ": ", deep + 1));
					bl.remove(k);
				} else {
					// Only a have.
					subs.add(formatPrint(a.get(k), k + ":", deep + 1, "+"));
				}
			}
			for (String k : bl) {
				// Only b have.
				subs.add(formatPrint(b.get(k), k + ":", deep + 1, "-"));
			}
			now.append('*').append(spaces(deep)).append(key).append("{\n");
			now.append(StringUtils.join(subs, ",\n"));
			now.append("\n*").append(spaces(deep)).append("}");
		} else if ((a.isArray()) && (b.isArray())) {
			// If both array, compare them one by one.
			subs = new ArrayList<StringBuilder>();
			List<JsonNode> al = getNodeList(a);
			List<JsonNode> bl = getNodeList(b);
			int i = 0;
			while ((i < al.size()) && (i < bl.size())) {
				subs.add(compareNode(al.get(i), bl.get(i), "", deep + 1));
				i++;
			}
			while (i < al.size()) {
				subs.add(formatPrint(al.get(i), "", deep + 1, "+"));
				i++;
			}
			while (i < bl.size()) {
				subs.add(formatPrint(bl.get(i), "", deep + 1, "-"));
				i++;
			}
			now.append('*').append(spaces(deep)).append(key).append("[\n");
			now.append(StringUtils.join(subs, ",\n"));
			now.append("\n*").append(spaces(deep)).append("]");
		} else {
			now.append(formatPrint(a, key, deep, "+"));
			now.append(",\n");
			now.append(formatPrint(b, key, deep, "-"));
		}
		return now;
	}

	/**
	 * Convert two string to json, compare them and print the difference.
	 * 
	 * @return Formated output.
	 */
	public static String compare(String a, String b) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode aj = mapper.readTree(a);
			JsonNode bj = mapper.readTree(b);
			return compareNode(aj, bj, "", 0).toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Compare two json node.
	 * 
	 * @return Formated outout.
	 */
	public static String compare(JsonNode a, JsonNode b) {
		return compareNode(a, b, "", 0).toString();
	}
}
