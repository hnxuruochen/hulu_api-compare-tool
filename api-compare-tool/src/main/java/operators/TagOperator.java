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

	public Tag addTag(String name, String user) {
		String q = "INSERT INTO tags(name, creator, time) VALUES (?, ?, CURRENT_TIMESTAMP());";
		template.update(q, name, user);
		q = "SELECT * FROM tags WHERE name = ?";
		return template.queryForObject(q, new Tag.TagMapper(), name);
	}

	public String getTagCreator(Integer id) {
		String q = "SELECT creator FROM tags WHERE id = ?";
		String output = null;
		try {
			output = template.queryForObject(q, String.class, id);
		} catch (DataAccessException e) {
		}
		return output;
	}

	public boolean existName(String name) {
		String q = "SELECT count(*) FROM tags WHERE name = ?";
		return template.queryForObject(q, Integer.class, name) > 0;
	}

	public Tag updateTag(Integer id, String name) {
		String q = "UPDATE tags SET name = ?, time = CURRENT_TIMESTAMP() WHERE id = ?";
		template.update(q, name, id);
		q = "SELECT * FROM tags WHERE id = ?";
		return template.queryForObject(q, new Tag.TagMapper(), id);
	}

	public void deleteTag(Integer id) {
		String q = "DELETE FROM tags WHERE id = ?";
		template.update(q, id);
	}
}
