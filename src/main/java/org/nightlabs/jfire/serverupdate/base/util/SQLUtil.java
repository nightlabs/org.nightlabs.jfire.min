package org.nightlabs.jfire.serverupdate.base.util;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public class SQLUtil 
{
	static final Map<Integer, String> JAVA_SQL_TYPES = new HashMap<Integer, String>();
	static {
		Field[] fields = Types.class.getFields();
		for (Field field : fields) {
			try {
				Integer value = (Integer) field.get(null);
				String name = field.getName();
				
				JAVA_SQL_TYPES.put(value, name);
			}
			catch (IllegalAccessException e) {
				//Do nothing
			}
		}
	}
	
	public static String getJdbcTypeName(int jdbcType) {
		return (String)JAVA_SQL_TYPES.get(jdbcType);
	}
	
	public static boolean isJavaSqlType(int columnType) {
		String type = (String) JAVA_SQL_TYPES.get(new Integer(columnType));
		if (type == null) {
			// it means it is a non-existing type
			return false;
		}
		else {
			return true;
		}
	}

	public static String getAvailableSqlTypes(Connection connection) throws Exception {
		ResultSet rs = null;
		try {
			DatabaseMetaData metaData = connection.getMetaData();
			if (metaData == null) {
				return null;
			}

			rs = metaData.getTypeInfo();
			
			StringBuffer sb = new StringBuffer();
			sb.append("<sqlTypes>");
			while (rs.next()) {
				String typeName = rs.getString("TYPE_NAME");
				
				short jdbcType = rs.getShort("DATA_TYPE");
				
				String jdbcTypeName = getJdbcTypeName(jdbcType);
				sb.append("<typeName>");
				sb.append(typeName);
				sb.append("</typeName>");
				sb.append("<dataType>");
				sb.append(jdbcType);
				sb.append("</dataType>");
				sb.append("<jdbcTypeName>");
				sb.append(jdbcTypeName);
				sb.append("</jdbcTypeName>");
			}
			sb.append("</sqlTypes>");
			return sb.toString();
		}
		finally {
			rs.close();
		}
	}
}