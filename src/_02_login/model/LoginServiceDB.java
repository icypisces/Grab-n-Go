package _02_login.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javax.naming.*;
import javax.sql.*;

import _00_init.*;
import _01_register.model.MemberBean;

public class LoginServiceDB implements LoginServiceDAO {
	static private List<MemberBean> memberList = new ArrayList<MemberBean>();
	private DataSource ds = null;

	public LoginServiceDB() throws NamingException, SQLException {
		Context ctx = new InitialContext();
		ds = (DataSource) ctx.lookup(GlobalService.JNDI_DB_NAME);
		if (memberList.isEmpty()) {
			populateMemberList(); // 此方法只會執行一次
		}
	}

	public MemberBean checkPassword(String user, String password) throws SQLException {
		String sql = "SELECT * From member where m_username = ? and m_password = ? ";
		Connection connection = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		MemberBean mb = null;
		try {
			connection = ds.getConnection();
			pStmt = connection.prepareStatement(sql);
			pStmt.setString(1, user);
			pStmt.setString(2, password);
			rs = pStmt.executeQuery();

			if (rs.next()) {
				String id = rs.getString("m_username").trim(); 
				// 必須確定rs.getString("memberID") not null
				String pswd = rs.getString("m_password").trim();
				String name = rs.getString("m_name").trim();
				String phone = rs.getString("m_phone").trim();
				String email = rs.getString("m_email").trim();
				String addr = rs.getString("m_address").trim();
				Date birthday = rs.getDate("m_birthday");
				Blob userImage = rs.getBlob("m_picture");
				String filename = rs.getString("m_filename").trim();
				mb = new MemberBean
						(id, pswd, name, phone, email, addr, birthday, userImage, filename);

			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (connection != null) {
				connection.close();
			}
		}
		return mb;
	}

	// 由Database讀取會員資料
	public void populateMemberList() throws SQLException {
		String sql = "SELECT * From member";
		Connection connection = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		try {
			connection = ds.getConnection();
			pStmt = connection.prepareStatement(sql);
			rs = pStmt.executeQuery();
			while (rs.next()) {
				String id = rs.getString("m_username").trim(); // 必須確定rs.getString("memberID") not null
				String pswd = rs.getString("m_password").trim();
				String name = rs.getString("m_name").trim();
				String phone = rs.getString("m_phone").trim();
				String email = rs.getString("m_email").trim();
				String addr = rs.getString("m_address").trim();
				Date birthday = rs.getDate("m_birthday");
				Blob userImage = rs.getBlob("m_picture");
				String filename = rs.getString("m_filename").trim();
				MemberBean mb = new MemberBean(id, pswd, name, phone, email, addr, birthday, userImage, filename);
				memberList.add(mb);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (connection != null)
				connection.close();
		}
	}

	public MemberBean checkIDPassword(String userId, String password) {
		// 檢查userId與password是否正確
		for (MemberBean mb : memberList) {
			if (mb.getMemberId().trim().equals(userId.trim())) {
				String encrypedString = GlobalService.encryptString(password.trim());
				String pswd = GlobalService.getMD5Endocing(encrypedString);
				String mbpswd = mb.getPassword().trim();
				if (mbpswd.equals(pswd)) {
					return mb;
				}
			}
		}
		return null;
	}

	public List<MemberBean> getMemberList() {
		return memberList;
	}

	public void addNewMember(MemberBean mb) {
		memberList.add(mb);
	}

}
