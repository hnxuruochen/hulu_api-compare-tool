package operators;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import entities.Tag;

public enum TagOperator {
	INSTANCE;
	private JdbcTemplate template = null;

	TagOperator() {
		template = Database.getTemplate();
	}

	public List<Tag> getAllTags() {
		String q = "SELECT * FROM tags;";
		return template.query(q, new Tag.TagMapper());
	}

	public Tag addTag(String newTag, String user) {
		String q = "INSERT INTO tags(name, creator, time) VALUES (?, ?, CURRENT_TIMESTAMP());";
		template.update(q, newTag, user);
		q = "SELECT * FROM tags WHERE name = ?";
		return template.queryForObject(q, new Tag.TagMapper(), newTag);
	}

	public String getTagCreator(String tag) {
		String q = "SELECT creator FROM tags WHERE name = ?";
		String output = null;
		try {
			output = template.queryForObject(q, String.class, tag);
		} catch (DataAccessException e) {
		}
		return output;
	}

	public Tag updateTag(String oldTag, String newTag) {
		String q = "UPDATE tags SET name = ?, time = CURRENT_TIMESTAMP() WHERE name = ?";
		template.update(q, newTag, oldTag);
		q = "SELECT * FROM tags WHERE name = ?";
		return template.queryForObject(q, new Tag.TagMapper(), newTag);
	}

	public void deleteTag(String tag) {
		String q = "DELETE FROM tags WHERE name = ?";
		template.update(q, tag);
	}
}
