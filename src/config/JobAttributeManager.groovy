package config

import groovy.sql.Sql

class JobAttributeManager {
	def jobIdentifier
	def envKey
	def serverAttributes = [:]
	def dbConfig
	def conf = [
		'local': [
			'url': 'dbdvadc46.na.averydennison.net/AutomationGahDevHUB',
			'dbName': 'AutomationGahDevHUB',
			'gpmDBUrl': "127.0.0.1",
			'gpmDBName': 'gpmdb',
			'GUID': 'sa',
			'GPWD': '5652218love?',
			
			'raw': true,
			'UID': 'gpmgmc',
			'PWD': 'Avery@123'
	],
	
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
	
	def setEnvKey(key){
		envKey = key	
		def serverAttrs = loadServerAttributes()
		IAObjectUtils.setServerAttrs(serverAttrs)
	}
	def testInDev(){
		setEnvKey("dev")
		return this;
	}
	def testInLocal(){
		setEnvKey("local")
		return this;	
	}
	
	def testInUat(){
		setEnvKey("uat")
		return this;
	}
	
	def testInStg(){
		envKey = "stg"
		setEnvKey(envKey)
		return this;
	}
	
	def testInProd(){
		envKey = "prod"
		setEnvKey(envKey)
		return this;
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
	
	def loadServerAttributes(myGpmDbConnectUrl=false){
		
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
		serverAttributes['rfid_s-reprint-db-user'] = envConf['UID']
		serverAttributes['rfid_s-reprint-db-pass'] = envConf['PWD']
		
		if(envConf['raw']) {
			serverAttributes['rfid_s-reprint-db-user'] = envConf['GUID']
			serverAttributes['rfid_s-reprint-db-pass'] = envConf['GPWD']
			serverAttributes = ['gpm-db':gpmDbConnectUrl = "jdbc:jtds:sqlserver://${envConf['gpmDBUrl']}:1433/${dbName};instance=SQLEXPRESS"]
		}else {
			serverAttributes = retrieveAttributes(sql)
			//serverAttributes.putAll(dbConfig)
			serverAttributes['rfid_s-reprint-db-user'] = envConf['UID']
			serverAttributes['rfid_s-reprint-db-pass'] = envConf['PWD']
		}
		
		
		
		
		return serverAttributes
	}
	
	def loadJobAttributes(jobIdentifier = '1035962'){
		if(serverAttributes == null){
			throw new RuntimeException("Server attributes have not been initialized yet!");	
		}
		
		//def jobIdentifier = '1292552'
		def sql = "SELECT\n" +
"	myatt.name,\n" +
"  myatt.type,\n" +
"	jv.[value],\n" +
"	jv.value_bool,\n" +
"	jv.value_date,\n" +
"	jv.value_double,\n" +
"	jv.value_int\n" +
"FROM\n" +
"	(\n" +
"		SELECT\n" +
"			att.id,\n" +
"			att.name,\n" +
"		  att.type\n" +
"		FROM\n" +
"			attributes att\n" +
"		WHERE\n" +
"			att.categories = 'job'\n" +
"	) AS myatt\n" +
"LEFT JOIN (\n" +
"	SELECT\n" +
"		j.id_attr,\n" +
"		j.[value],\n" +
"		j.value_bool,\n" +
"		j.value_date,\n" +
"		j.value_double,\n" +
"		j.value_int\n" +
"	FROM\n" +
"		job_values j\n" +
"	JOIN job_names jn ON j.id_pao = jn.id\n" +
"	WHERE\n" +
"		jn.name LIKE '%${jobIdentifier}'\n" +
") AS jv ON myatt.id = jv.id_attr"
		
	    /*if(envKey.equals("prod")){
			serverAttributes['rfid_s-reprint-db-user'] = "gpmgmc"
			serverAttributes['rfid_s-reprint-db-pass'] = "Avery@123"
		}*/
		def attrs = retrieveAttributes(sql)
	    IAObjectUtils.setJobAttrs(attrs)
		return attrs
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
		
		def envConf = conf[envKey];
		if(!envConf['raw']) {
			databaseName = dnsFix(databaseName)
		}
		
		
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
