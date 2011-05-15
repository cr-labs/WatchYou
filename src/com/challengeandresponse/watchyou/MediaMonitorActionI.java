package com.challengeandresponse.watchyou;



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
 * and will pass one MediaRecord at a time to your class's process() method. Your process method
 * must return true if the record was changed, false otherwise. If true is returned, the 
 * caller will update the record in the database.
 * 
 * @author jim
 */


public interface MediaMonitorActionI {

	/**
	 * Get the selector, either a db4o Predicate or a Vector of Strings that are mediaIDs
	 */
	public Object getSelector();


	/**
	 * Process a Media Record...
	 * Return value is used by the caller to determine if the MediaRecord needs to be updated in the database
	 * @return true if the last call to processMR() altered the passed-in MediaRecord; false otherwise
	 */
	public boolean process(MediaRecord mr);




}