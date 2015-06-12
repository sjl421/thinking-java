package org.cneng.httpclient;

import ch17database.Record;

import java.io.FileInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 15-6-12
 * Time: 上午11:24
 * JdbcUtils
 */
public class JdbcUtils {
    /**
     * 测试JdbcUtils的各个方法
     *
     */
    public static void main(String[] args) throws Exception {
        Connection connection = getConnection();

        /******************测试插入操作*****************/
        String insertSql = "insert into record(description,content,createdTime,modifyTime) values (?,?,?,?)";
        for (int i = 0; i < 100; i++) {
            List<String> insertParam = new ArrayList<String>();
            insertParam.add("this is record " + i);
            insertParam.add("record " + i + "content");
            insertParam.add("2012-07-04");
            insertParam.add("2012-07-04");
            updateDataByJdbc(connection, insertSql, insertParam);
        }

        /******************测试更新操作*****************/
        String updateSql = "update record set description=? where id=?";
        List<String> updateParam = new ArrayList<String>();
        updateParam.add("update update update");
        updateParam.add("1");
        updateDataByJdbc(connection, updateSql, updateParam);

        /******************测试删除操作*****************/
        String deleteSql = "delete from record where id=?";
        List<String> deleteParam = new ArrayList<String>();
        deleteParam.add("2");
        updateDataByJdbc(connection, deleteSql, deleteParam);

        /******************测试数目查询操作*****************/
        String countSql = "select count(*) from record where content like ? ; ";
        List<String> countParam = new ArrayList<String>();
        countParam.add("%cont%");
        System.out.println("查询总数目为" + queryCountByJdbc(connection, countSql, countParam));

        /******************测试普通查询操作*****************/
        String querySql = "select * from record where content like ?";
        List<String> queryParam = new ArrayList<String>();
        queryParam.add("%cont%");
        List<Record> commonList = queryDataByJdbc(connection, querySql, queryParam);
        System.out.println("普通查询结果：");
        for (Record r : commonList) {
            System.out.println(r);
        }

        /******************测试分页查询操作*****************/
        String pageQuerySql = "select * from record where content like ?";
        List<String> pageQueryParam = new ArrayList<String>();
        pageQueryParam.add("%cont%");
        List<Record> pageList = queryPageDataByJdbc(connection, pageQuerySql, pageQueryParam, 20, 20);
        System.out.println("分页查询结果：");
        for (Record r : pageList) {
            System.out.println(r);
        }
        if (connection != null) {
            connection.close();
        }
    }

    /**
     * 初始化IDMAP
     * @param map
     */
    public static void startIdMap(Map<String, String> map) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlText = "SELECT name, sid FROM t_idmap;";
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sqlText);
            rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("name"), rs.getString("sid"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 更新IDMAP
     * @param map
     */
    public static void endIdMap(Map<String, String> map) {
        Connection conn = null;
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;
        String selectSQL = "SELECT COUNT(*) FROM t_idmap WHERE name=?";
        String insertSQL = "INSERT INTO t_idmap(name, sid) VALUES(?,?)";
        String updateSQL = "UPDATE t_idmap SET sid=? WHERE name=?";
        try {
            conn = getConnection();
            for (String k : map.keySet()) {
                ps1 = conn.prepareStatement(selectSQL);
                ps1.setString(1, k);
                int count = 0;
                ResultSet rs = ps1.executeQuery();
                if (rs.next()) {
                    count = rs.getInt(1);
                }
                if (count == 0) {
                    ps2 = conn.prepareStatement(insertSQL);
                    ps2.setString(1, k);
                    ps2.setString(2, map.get(k));
                    ps2.executeUpdate();
                } else {
                    ps3 = conn.prepareStatement(updateSQL);
                    ps3.setString(1, map.get(k));
                    ps3.setString(2, k);
                    ps3.executeUpdate();
                }
                ps1.close();
                if (ps2 != null) {
                    ps2.close();
                }
                if (ps3 != null) {
                    ps3.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps1 != null) {
                    ps1.close();
                }
                if (ps2 != null) {
                    ps2.close();
                }
                if (ps3 != null) {
                    ps3.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取数据库连接
     *
     * @return 数据库连接
     * @throws Exception
     */
    private static Connection getConnection() throws Exception {
        Connection conn = null;
        try {
            Properties props = new Properties();
            FileInputStream jdbcConfig = new FileInputStream("src/main/resources/jdbc.properties");
            props.load(jdbcConfig);
            jdbcConfig.close();

            String driver = props.getProperty("jdbc.driver");
            if (driver != null) {
                Class.forName(driver);
            }
            String url = props.getProperty("jdbc.url");
            String username = props.getProperty("jdbc.username");
            String password = props.getProperty("jdbc.password");
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Database connection established");
        } catch (Exception e) {
            System.err.println("Cannot connect to database server");
            if (conn != null) {
                conn.close();
                System.out.println("Database connection terminated");
                return null;
            }
        }
        return conn;
    }

    /**
     * 数目查询操作
     *
     * @param conn    数据库连接
     * @param sqlText sql文
     * @param params  参数列表
     * @return 查询数目
     * @throws Exception
     */
    private static int queryCountByJdbc(Connection conn, String sqlText, List<String> params) throws Exception {
        int result = 0;
        PreparedStatement ps = conn.prepareStatement(sqlText);
        int index = 1;
        for (String p : params) {
            ps.setString(index++, p);
        }
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            result = rs.getInt(1);
        }
        rs.close();
        ps.close();
        return result;
    }

    /**
     * 普通查询操作
     *
     * @param conn    数据库连接
     * @param sqlText sql文
     * @param params  参数列表
     * @return 检索结果Record记录
     * @throws Exception
     */
    private static List<Record> queryDataByJdbc(
            Connection conn, String sqlText, List<String> params) throws Exception {
        PreparedStatement ps = conn.prepareStatement(sqlText);
        int index = 1;
        for (String p : params) {
            ps.setString(index++, p);
        }
        ResultSet rs = ps.executeQuery();
        // do something....
        List<Record> records = new ArrayList<Record>();
        while (rs.next()) {
            Record record = new Record();
            record.setId(rs.getLong("id"));
            record.setDescription(rs.getString("description"));
            record.setContent(rs.getString("contENt"));
            record.setCreatedTime(rs.getDate("createdtime"));
            record.setModifyTime(rs.getTimestamp("modifytime"));
            records.add(record);
        }
        rs.close();
        ps.close();
        return records;
    }

    /**
     * 分页查询操作
     *
     * @param conn    数据库连接
     * @param sqlText sql文
     * @param params  参数列表
     * @param offset  偏移量
     * @param max     页面最大值
     * @return 检索结果Record记录
     * @throws Exception
     */
    private static List<Record> queryPageDataByJdbc(Connection conn, String sqlText,
                                                    List<String> params, long offset, int max) throws Exception {
        PreparedStatement ps = conn.prepareStatement(sqlText + " LIMIT " + offset + ", " + max);
        int index = 1;
        for (String p : params) {
            ps.setString(index++, p);
        }
        ResultSet rs = ps.executeQuery();
        // do something....
        List<Record> records = new ArrayList<Record>();
        while (rs.next()) {
            Record record = new Record();
            record.setId(rs.getLong("id"));
            record.setDescription(rs.getString("description"));
            record.setContent(rs.getString("contENt"));
            record.setCreatedTime(rs.getDate("createdtime"));
            record.setModifyTime(rs.getTimestamp("modifytime"));
            records.add(record);
        }
        rs.close();
        ps.close();
        return records;
    }

    /**
     * 增删改操作
     *
     * @param conn    数据库连接
     * @param sqlText sql文
     * @param params  参数列表
     * @return 增删改成功数目
     */
    private static int updateDataByJdbc(Connection conn, String sqlText, List<String> params) throws Exception {
        PreparedStatement ps = conn.prepareStatement(sqlText);
        int index = 1;
        for (String p : params) {
            ps.setString(index++, p);
        }
        int count = ps.executeUpdate();
        ps.close();
        return count;
    }
}
