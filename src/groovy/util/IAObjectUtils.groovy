package groovy.util

import java.util.Map

class IAObjectUtils {
	
	static def setServerAttrs(map){
		loadAttr(server, map)
	}
	
	static def setJobAttrs(map){
		loadAttr(job, map)
	}
	
	def createJobSpoolDirPath(serverRoot, slotNumber, jobIdentifier){
		def currentJobSpoolDir =  serverRoot + '/spool/slot' + slotNumber + '/' + jobIdentifier
		return currentJobSpoolDir
	}
	
	static def createJob(map){
		def serverRoot = server.'server-root'
		def serverSpoolDir = serverRoot + '/spool/slot'
		def mainJobSpoolDir = serverSpoolDir + map['job-spool-lot'] + '/' + map['job-identifier']
		def file = new File(mainJobSpoolDir)
		if(!file.exists()){
			file.mkdirs()
		}
		
		loadAttr(job, map)
	}
	
	static def loadAttr(iaObject, Map map){
		def st = iaObject.metaClass.static
		map.each {
			def getter = 'get' + it.key.toString().capitalize()
			def val = it.value
			st[getter] = {
				return val
			}
		}
	}
}
