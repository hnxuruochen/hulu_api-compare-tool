package operators;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import entities.Tag;

/**
 * @author ruochen.xu
 */
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

	public Integer addTag(String name, String user) {
		String q = "INSERT INTO tags(name, creator, time) VALUES (?, ?, CURRENT_TIMESTAMP());";
		KeyHolder id = new GeneratedKeyHolder();
		try {
			template.update(new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(Connection con)
						throws SQLException {
					PreparedStatement ps = con.prepareStatement(q,
							Statement.RETURN_GENERATED_KEYS);
					ps.setString(1, name);
					ps.setString(2, user);
					return ps;
				}
			}, id);
		} catch (DuplicateKeyException e) {
			return null;
		}
		return id.getKey().intValue();
	}

	public Tag getTagById(int id) {
		String q = "SELECT * FROM tags WHERE id = ?";
		Tag tag = null;
		try {
			tag = template.queryForObject(q, new Tag.TagMapper(), id);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
		return tag;
	}

	public boolean updateTag(Integer id, String name) {
		String q = "UPDATE tags SET name = ?, time = CURRENT_TIMESTAMP() WHERE id = ?";
		try {
			template.update(q, name, id);
		} catch (DuplicateKeyException e) {
			return false;
		}
		return true;
	}

	public void deleteTag(Integer id) {
		String q = "DELETE FROM tags WHERE id = ?;";
		template.update(q, id);
		q = "UPDATE tasks SET tag_id = 1 WHERE tag_id = ?;";
		template.update(q, id);
	}
}
