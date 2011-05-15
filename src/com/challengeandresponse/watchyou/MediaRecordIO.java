package com.challengeandresponse.watchyou;

import java.util.*;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Predicate;

/**
 * IO Methods for YouTubeRecord objects. Every Session should have one of these for its own record IO
 * @author jim
 */
public class MediaRecordIO {

	private ObjectContainer mediaDB;
	private ObjectContainer lockDB;
	
	private static final int WAIT_FOR_UPDATE_LOCK_MSEC = 2000;

	public MediaRecordIO(ObjectContainer mediaDB,ObjectContainer lockDB) {
		this.mediaDB = mediaDB;
		this.lockDB = lockDB;
	}

	
	/**
	 * @param videoID the video id to check for presence in the database
	 * @return a WyYouTubeRecord from the database, for the specified videoID, or null if the record is not in the database
	 */
	public MediaRecord getMediaRecord(String mediaID) {
		MediaRecord mr = new MediaRecord(null);
		mr.mediaID = mediaID;
		ObjectSet <MediaRecord> result = mediaDB.get(mr);
		if (! result.hasNext())
			return null;
		else
			return result.next();
	}

	

	/**
	 * Get a list of tracked videos / allow searching the list
	 * @return A composed-for-display list of tracked media, with table tags (TR and TD ONLY) around the fields
	 */
	public List<MediaRecord> getMediaRecords(MediaRecord prototype) {
		return mediaDB.get(prototype);
	} 
	
	
	public List <MediaRecord> getMediaRecords(Predicate <MediaRecord> predicate) {
		List <MediaRecord> mr = mediaDB.query(predicate);
		return mr;
	}

	public List <MediaRecord> getMediaRecords(Vector <String> mediaIDs) {
		List <MediaRecord> result = new ArrayList <MediaRecord> ();
		Iterator <String> i = mediaIDs.iterator();
		while (i.hasNext()) {
			Object o = i.next();
			if (! (o instanceof String)) // omit all non-strings
				continue;
			MediaRecord mr = getMediaRecord((String) o);
			if (mr != null) // omit missing media records or invalid mediaIDs
				result.add(mr);
		}
		return result;
	}
	
	
	
	/**
	 * Add a new MediaRecord if it does not already exist.
	 * This method synchronizes on the lockDB provided in the constructor, to assure that
	 * simultaneous inserts of identical data do not occur
	 * @param mr the MediaRecord to add to the database
	 * @return true if the record was added, false if not
	 */
	public boolean addMediaRecord(MediaRecord mr) {
		System.out.println("Mrio.addMediaRecord, size of history is: "+mr.statusReports.size());
		// attempt to set a semaphore over the mediaID in this YTR
		// if we can't get a semaphore, someone else is adding this video right now
		// otherwise if this returns true, then we have a semaphore
		if (! lockDB.ext().setSemaphore(mr.mediaID,0))
			return false;

		try {
			// try to add the record, if one with its mediaID is not already on file
			if (getMediaRecord(mr.mediaID) == null) {
				mediaDB.set(mr);
				mediaDB.commit();
				return true;
			}
			else
				return false;
		}
		finally {
			lockDB.ext().releaseSemaphore(mr.mediaID);
		}
	}


	
	/**
	 * Update a MediaRecord (which must already exist)
	 * This method synchronizes on the lockDB provided in the constructor, to assure that
	 * simultaneous inserts of identical data do not occur
	 * @param mr the MediaRecord to add to the database
	 * @return true if the record was added, false if not
	 */
	public boolean updateMediaRecord(MediaRecord mr) {
		System.out.println("Mrio.updateMediaRecord, size of history is: "+mr.statusReports.size());
		// attempt to set a semaphore over the mediaID in this YTR
		// if we can't get a semaphore, someone else is updating this video right now
		// otherwise if this returns true, then we have a semaphore
		if (! lockDB.ext().setSemaphore(mr.mediaID,WAIT_FOR_UPDATE_LOCK_MSEC)) {
			System.out.println("1");
			return false;
		}

		try {
			// try to add the record, if one with its mediaID is not already on file
			if (getMediaRecord(mr.mediaID) != null) {
//				mediaDB.ext().set(mr,3); // well, this also worked but i sure don't like it. now using some configure calls on container opening back in WatchYou to (apparently) accomplish the same thing
				mediaDB.set(mr);
				mediaDB.commit();
				return true;
			}
			else {
				return false;
			}
		}
		finally {
			lockDB.ext().releaseSemaphore(mr.mediaID);
		}
	}

	
} // end of class MediaRecordIO
