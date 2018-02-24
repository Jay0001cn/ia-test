package config

import groovy.sql.Sql

class JobAttributeManager {
	def jobIdentifier
	def envKey
	def serverAttributes
	def dbConfig
	def conf = [
		'dev': [
				'url': 'dbdvadc46.na.averydennison.net/AutomationGahDevHUB',
				'dbName': 'AutomationGahDevHUB',
				'gpmDBUrl': 'dbdvadc46.na.averydennison.net/GPMEAH',
				'gpmDBName': 'DEV_GPM_DB',
				'UID': 'gpmgmc',
				'PWD': 'Avery@123'
		],

		'uat': [
				'url': 'dbdvadc46.na.averydennison.net/AutomationGahUatHUB',
				'dbName': 'AutomationGahUatHUB',
				'gpmDBUrl': 'dbdvadc46.na.averydennison.net/GPMEAH',
				'gpmDBName': 'UAT_GPM_DB',
				'UID': 'gpmgmc',
				'PWD': 'Avery@123'
		],

		'stg': [
				'url': 'clpdadc35.na.averydennison.net/GPM_Stage_Automation',
				'dbName': 'GPM_Stage_Automation',
				'gpmDBUrl': 'dbdvadc46.na.averydennison.net/GPMEAH',
				'gpmDBName': 'STG_GPM_DB',
				'UID': 'gpmgmc',
				'PWD': 'Avery@123'
		],

		'prod': [
				'url': 'clpdadc35.na.averydennison.net/GPM_Automation',
				'dbName': 'GPM_Automation',
				'gpmDBUrl': 'clpdadc35.na.averydennison.net/GPMPRD',
				'gpmDBName': 'AutomationGahDevHUB',
				'UID': 'gpmgmc',
				'PWD': 'Avery@123'
		],
	];
	
	def testInDev(){
		envKey = "dev"	
	}
	
	def testInUat(){
		envKey = "uat"
	}
	
	def testInStg(){
		envKey = "stg"
	}
	
	def testInProd(){
		envKey = "prod"
	}
	
	def typeValueKeyMap = [
		'String': 'value',
		'ObjectIdentifier': 'value',
		'DocOrJobIdentifier': 'value',
		'Enumeration': 'value',
		'Password': 'value',
		
		'DateTime': 'value_date',
		
		'Integer': 'value_int',
		'Long': 'value_int',
		'Cardinal': 'value_int',
		'TimeInterval': 'value_int',
		
		'Boolean': 'value_bool',
		'Double': 'value_double',
	]
	
	def loadServerAttributes(){
		
		if(serverAttributes != null){
			return serverAttributes
		}
		
		def sql = "SELECT\n" +
					"	a.name,\n" +
					"	a.type,\n" +
					"	jv.[value],\n" +
					"	jv.[value_int],\n" +
					"	jv.[value_bool],\n" +
					"	jv.[value_date],\n" +
					"	jv.[value_double]\n" +
					"FROM\n" +
					"	server_values jv\n" +
					"LEFT JOIN attributes a ON jv.id_attr = a.id";
					
		def envConf = conf[envKey];
		
		def dbName = envConf['dbName']
		def dbUrl = envConf['url']
		def gpmDbConnectUrl = "jdbc:jtds:sqlserver://${dbUrl}:1433/${dbName}"
		
		dbConfig = [
			'gpm-db': gpmDbConnectUrl,
			'rfid_s-reprint-db-user': envConf['UID'],
			'rfid_s-reprint-db-pass': envConf['PWD']
		]
		
		serverAttributes = retrieveAttributes(sql)
		
		return serverAttributes
	}
	
	def loadJobAttributes(){
		if(serverAttributes == null){
			throw new RuntimeException("Server attributes have not been initialized yet!");	
		}
		
		def sql =   "SELECT\n" +
				"	a.name,\n" +
				"	a.type,\n" +
				"	jv.[value],\n" +
				"	jv.[value_int],\n" +
				"	jv.[value_bool],\n" +
				"	jv.[value_date],\n" +
				"	jv.[value_double]\n" +
				"FROM\n" +
				"	job_values jv\n" +
				"LEFT JOIN attributes a ON jv.id_attr = a.id\n" +
				"JOIN job_names jn ON jv.id_pao = jn.id\n" +
				"WHERE\n" +
				"	jn.name LIKE '%${jobIdentifier}'\n" +
				"order by a.name"
		
	    /*if(envKey.equals("prod")){
			serverAttributes['rfid_s-reprint-db-user'] = "gpmgmc"
			serverAttributes['rfid_s-reprint-db-pass'] = "Avery@123"
		}*/
		
		return retrieveAttributes(sql)
	}

	private retrieveAttributes(String sql) {
		
		def rows = retrieveRows(sql)

		return rows.collectEntries {
			def type = it['type']
			def valueKey = typeValueKeyMap[type];
			def value = it[valueKey]
			def name = it['name']
			
			if(name.equals('gpm-db')){
				value = dnsFix(value)
			}
			
			if(name.equals('rfid_s-reprint-db-pass') && envKey.equals("prod")){
				value = 'Avery@123'
			}
			
			return [(name): value]
		}
	}

	def openSession() {

		def databaseName = dbConfig.'gpm-db'
		
		databaseName = dnsFix(databaseName)
		
		def username = dbConfig."rfid_s-reprint-db-user"
		def password = dbConfig."rfid_s-reprint-db-pass"
		def connectionStr = 'net.sourceforge.jtds.jdbc.Driver'
		return Sql.newInstance(databaseName, username, password, connectionStr)
	}

	private String dnsFix(String databaseName) {
		def suffix = ".na.averydennison.net"
		if(!databaseName.contains(suffix)){
			def parts = databaseName.split(":")
			def newParts = parts[0] + ":" + parts[1] + ":" + parts[2] + ":" + parts[3] + suffix + ":" + parts[4]

			databaseName = newParts
		}
		return databaseName
	}

	def retrieveRows(sql) {
		def session
		try {
			session = openSession()
			return session.rows(sql)
		} catch (Exception e) {
			throw new RuntimeException("Database error!", e)
		} finally {
			if(session != null){
				session.close()
			}
		}
	}
}
