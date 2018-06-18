package groovy.lang

class server {

	public static String submitJob(queue, closer){
		println 'Submitting new job to queue - ' + queue
		def job = ['TYPE': '', 'po_originalJobId': '']
		closer(job)
		
		return '1005';
	}
	
	def static getObject(type, paoId){
		return ['job-retention-period':1000];
	}
}
