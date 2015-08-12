package entities;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class Tag {

	public static class TagMapper implements RowMapper<Tag> {
		@Override
		public Tag mapRow(ResultSet rs, int rowNum) throws SQLException {
			Tag tag = new Tag();
			tag.setId(rs.getInt("id"));
			tag.setName(rs.getString("name"));
			tag.setCreator(rs.getString("creator"));
			tag.setTime(rs.getDate("time") + "  " + rs.getTime("time"));
			return tag;
		}
	}

	private Integer id = null;
	private String name = null;
	private String creator = null;
	private String time = null;

	public void setId(Integer id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public void setTime(String time) {
		this.time = time;
	}
	
	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getCreator() {
		return creator;
	}

	public String getTime() {
		return time;
	}
}
