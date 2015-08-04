package comparator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

public class extendedJsonComparator {
	private static int TAB_LEN = 4;

	private boolean objectOrder = false;
	private boolean arrayOrder = true;

	private Comparator<JsonNode> nodeComparator = new Comparator<JsonNode>() {
		private int typeNumber(JsonNode node) {
			if (node.isObject()) {
				return 2;
			} else if (node.isArray()) {
				return 1;
			} else {
				return 0;
			}
		}

		@Override
		public int compare(JsonNode a, JsonNode b) {
			int ans = typeNumber(a) - typeNumber(b);
			if (ans != 0) {
				return ans;
			}
			if (a.isObject()) {
				List<String> al = getKeyList(a);
				List<String> bl = getKeyList(b);
				ans = al.size() - bl.size();
				if (ans != 0) {
					return ans;
				}
				int i = 0;
				while ((i < al.size()) && (i < bl.size())) {
					ans = al.get(i).compareTo(bl.get(i));
					if (ans != 0) {
						return ans;
					}
					ans = compare(a.get(al.get(i)), b.get(bl.get(i)));
					if (ans != 0) {
						return ans;
					}
				}
			} else if (a.isArray()) {
				List<JsonNode> al = getNodeList(a);
				List<JsonNode> bl = getNodeList(b);
				ans = al.size() - bl.size();
				if (ans != 0) {
					return ans;
				}
				int i = 0;
				while ((i < al.size()) && (i < bl.size())) {
					ans = compare(al.get(i), bl.get(i));
					if (ans != 0) {
						return ans;
					}
				}
			}
			return a.toString().compareTo(b.toString());
		}
	};

	public extendedJsonComparator() {
	}

	public extendedJsonComparator(boolean arrayOrder) {
		this.arrayOrder = arrayOrder;
	}

	public extendedJsonComparator(boolean objectOrder, boolean arrayOrder) {
		this.objectOrder = objectOrder;
		this.arrayOrder = arrayOrder;
	}

	/**
	 * Generate spaces.
	 * 
	 * @param deep
	 *            Deep in json.
	 * @return TAB_LEN * deep spaces.
	 */
	private String spaces(int deep) {
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
	private List<String> getKeyList(JsonNode json) {
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
	private List<JsonNode> getNodeList(JsonNode json) {
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
	private StringBuilder formatPrint(JsonNode node, String key, int deep,
			String prefix) {
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
			now.append(StringUtils.join(subs, ",\n")).append("\n");
			now.append(prefix).append(spaces(deep)).append("}");
		} else if (node.isArray()) {
			now.append("[\n");
			subs = new ArrayList<StringBuilder>();
			for (JsonNode j : getNodeList(node)) {
				subs.add(formatPrint(j, "", deep + 1, prefix));
			}
			now.append(StringUtils.join(subs, ",\n")).append("\n");
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
	private StringBuilder compareNode(JsonNode a, JsonNode b, String key,
			int deep) {
		List<StringBuilder> subs = null;
		StringBuilder now = new StringBuilder();
		if (nodeComparator.compare(a, b) == 0) {
			// Normally print if equal.
			now = formatPrint(a, key, deep, " ");
		} else if ((a.isObject()) && (b.isObject())) {
			// If both object, compare them according the keys.
			subs = new ArrayList<StringBuilder>();
			List<String> al = getKeyList(a);
			List<String> bl = getKeyList(b);
			if (objectOrder) {
				int ap = 0;
				int bp = 0;
				while ((ap < al.size()) || (bp< bl.size())) {
					int com = 0;
					if (ap >= al.size()) {
						com = 1;
					} else if (bp>=bl.size()) {
						com = -1;
					} else {
						com = al.get(ap).compareTo(bl.get(bp));
					}
					if (com == -1) {
						subs.add(formatPrint(a.get(al.get(ap)), al.get(ap) + ":", deep + 1, "+"));
						ap++;
					} else if (com == 1) {
						
					}
					
					
				}
			}
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
			if (!arrayOrder) {
				// IF unordered, clear the common object first.
				List<JsonNode> al2 = new ArrayList<JsonNode>(al);
				for (JsonNode j : al2) {
					if (bl.contains(j)) {
						subs.add(formatPrint(j, "", deep + 1, " "));
						bl.remove(j);
						al.remove(j);
					}
				}
			}
			char prefix = ' ';
			if ((al.size() != 0) || (bl.size() != 0)) {
				prefix = '*';
			}
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
			now.append(prefix).append(spaces(deep)).append(key).append("[\n");
			now.append(StringUtils.join(subs, ",\n"));
			now.append("\n").append(prefix).append(spaces(deep)).append("]");
		} else {
			now.append(formatPrint(a, key, deep, "+"));
			now.append(",\n");
			now.append(formatPrint(b, key, deep, "-"));
		}
		return now;
	}

	private JsonNode rebuildJsonNode(JsonNode node) {
		if (node.isObject()) {
			ObjectNode n = JsonNodeFactory.instance.objectNode();
			List<String> nl = getKeyList(node);
			if (objectOrder) {
				Collections.sort(nl);
			}
			for (String name : nl) {
				n.set(name, rebuildJsonNode(node.get(name)));
			}
			return n;
		} else if (node.isArray()) {
			ArrayNode n = JsonNodeFactory.instance.arrayNode();
			List<JsonNode> nl = getNodeList(node);
			for (int i = 0; i < nl.size(); i++) {
				nl.set(i, rebuildJsonNode(nl.get(i)));
			}
			if (arrayOrder) {
				Collections.sort(nl, nodeComparator);
			}
			for (JsonNode j : nl) {
				n.add(j);
			}
			return n;
		} else {
			return node;
		}
	}

	public String work(String a, String b) throws JsonProcessingException,
			IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode aj = mapper.readTree(a);
		JsonNode bj = mapper.readTree(b);
		aj = rebuildJsonNode(aj);
		bj = rebuildJsonNode(bj);
		return compareNode(aj, bj, "", 0).toString();
	}

	/*public static void main(String[] args) throws JsonProcessingException,
			IOException {
		String a = "{\"d\": 1,\"c\": 2,\"b\": 3,\"a\": 4}";
		String b = "{\"a\": 4,\"b\": 3,\"c\": 2,\"d\": 1}";
		ObjectMapper mapper = new ObjectMapper();
		JsonNode aj = mapper.readTree(a);
		JsonNode bj = mapper.readTree(b);
		// String output = new JsonComparator().compareNode(aj, bj, "", 0,
		// false)
		// .toString();
		// String output = new JsonComparator().formatPrint(aj, "", 0,
		// "").toString();
		// System.out.println(output);
		// System.out.println(aj.hashCode());
		// System.out.println(bj.hashCode());
		LinkedHashMap<String, Integer> map = new LinkedHashMap<String, Integer>();
		map.put("a", 1);
		map.put("d", 4);
		map.put("b", 2);
		map.put("c", 3);
		LinkedHashMap<String, Integer> map2 = new LinkedHashMap<String, Integer>();
		map2.put("a", 1);
		map2.put("b", 2);
		map2.put("c", 3);
		map2.put("d", 4);
		System.out.println(map.hashCode());
		System.out.println(map2.hashCode());
		LinkedHashMap<JsonNode, Integer> m = new LinkedHashMap<JsonNode, Integer>();
		m.put(aj, 12213123);
		System.out.println(m.get(bj));
	}*/
}
