package com.challengeandresponse.watchyou;

import com.challengeandresponse.eventlogger.DBEventLogger;
import com.db4o.query.Predicate;


/**
 * MediaMonitorAction objects contain:<br />
 * 1. a means of selecting records to process, either a db4o Predicate, or a Vector of mediaIDs<br />
 * 2. a process() method that is passed each MediaRecord in turn, operates on it, and stores it back<br />
 * 
 * The contract with the container that runs this:
 * The container handles all database io
 * 
 * Provide in your object all the resources it needs to update records, such as data retrieval hooks
 * 
 * The container will use the class's Selector (a db4o Predicate, or a list of mediaIDs) 
 * and will pass one MediaRecord at a time to your class's process() method
 * 
 * After each record it will call the changed() method of your class, which should return true
 * if the MediaRecord was changed. If the record was changed, the container will update the
 * MediaRecord in the database.
 * 
 * 
 * @author jim
 *
 */


public class MMUpdateAllRecords implements MediaMonitorActionI {
	
	YouTubeREST ytRest = null;
	DBEventLogger dbel = null;
	
	MMUpdateAllRecords(YouTubeREST ytRest, DBEventLogger sel) {
		this.ytRest = ytRest;
		this.dbel = sel;
	}
	
	
	
	/**
	 * Make the selector a db4o Predicate
	 */
	public Predicate getSelector() {
		return new AllPredicate();
	}		
	class AllPredicate extends Predicate <MediaRecord> {
		private static final long serialVersionUID = 1L;
		public boolean match(MediaRecord mr) {
			return true;
		}
	}

	
		
	
	/**
	 * Process a Media Record...
	 */
	public boolean process(MediaRecord mr) {
		dbel.addEvent("in MMUpdateAllRecords.process()");
		try {
			dbel.addEvent("calling ytRest.fetchVideoById");
			YouTubeRecord newRec = ytRest.fetchVideoById(mr.mediaID,false);
			mr.statusReports.add(newRec.getFirstStatus());
		}
		catch (WatchYouException wye) {
			dbel.addEvent("Exception in MMUpdateAllRecords: "+wye.getMessage());
			return false;
		}
		dbel.addEvent("returning true from MMUpdateAllRecords.process()");
		return true;
	}
	
	
	
}