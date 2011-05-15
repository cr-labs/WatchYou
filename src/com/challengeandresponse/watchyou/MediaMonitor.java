package com.challengeandresponse.watchyou;

import java.util.*;

import com.challengeandresponse.eventlogger.DBEventLogger;
import com.db4o.query.Predicate;

public class MediaMonitor extends Thread {

	// the mrio handle used by this task
	private MediaRecordIO mrio;

	// a collection of pending queries to select records to examine
	private Vector <TaskRecord> tasks;

	// the name of this MediaMonitor so it can be ID'd in logs and other records
	private String name;
	// milliseconds to sleep between individual records (dont query the source too fast)
	private long sleepMsec;
	// milliseconds to sleep between queries
	private long loopSleepMsec;
	// a ServerEventLogger for writing updates about what's going on
	private DBEventLogger dbel;
	// if true, the thread is running. set to false to terminate. This should be set false by shutdown() which also calls interrupt() to break any sleeps that are happening
	private boolean running;

	/**
	 * @param name			A lable for this MediaMonitor so it can be ID'd in logs and elsewhere
	 * @param mrio			MediaRecordIO handle for this thread to use
	 * @param sleepMsec		milliseconds to sleep between updates of individual records
	 * @param loopSleepMsec	milliseconds to sleep between full loops of the query
	 */
	public MediaMonitor(String name, MediaRecordIO mrio, long sleepMsec, long loopSleepMsec, DBEventLogger sel) {
		this.name = name;
		this.mrio = mrio;
		this.sleepMsec = sleepMsec;
		this.loopSleepMsec = loopSleepMsec;
		this.dbel = sel;
		tasks = new Vector <TaskRecord> ();
	}

	/**
	 * @param mmai a class that implements the MediaMonitorActionI interface
	 */
	public void addOneshotTask(MediaMonitorActionI mmai) {
		tasks.add(new TaskRecord(mmai,false));
	}

	/**
	 * @param mmai a class that implements the MediaMonitorActionI interface
	 */
	public void addRepeatingTask(MediaMonitorActionI mmai) {
		tasks.add(new TaskRecord(mmai,true));
	}

	public boolean queueEmpty() {
		return (tasks.size() < 1);
	}

	public void shutdown() {
		this.running = false;
		this.interrupt();
	}


	@SuppressWarnings("unchecked")
	public void run() {
		addEvent("Started");
		running = true;

		while (running) {
			addEvent("Sleeping "+loopSleepMsec+" msec");
			// SLEEP between runs. We start out sleeping...
			try { 
				Thread.sleep(loopSleepMsec); 
			}
			catch (InterruptedException ie) { 
				// if we terminated the sleep with an interrupt from shutdown(), quit the while loop
				if (! running)
					break;  
			}
			// if there is nothing to do just loop back, wait, and check again later
			if (queueEmpty())
				continue;

			addEvent("Found tasks. Getting tasks iterator. Task count:"+tasks.size());
			Iterator <TaskRecord> taskI = tasks.iterator();
			// if not running, bail, else fetch a task and keep going
			while (taskI.hasNext() && running) {
				// otherwise, queries are available. Grab one and run it
				addEvent("Retrieving next task");
				TaskRecord tr = taskI.next();
				MediaMonitorActionI mmai = tr.mmai;
				if (! tr.repeat)
					taskI.remove();

				List <MediaRecord> workSet = null; // the working set will be stored here

				// if this was a Predicate, build the object collection here
				if (mmai.getSelector() instanceof Predicate) {
					addEvent("Predicate found. Will run native query.");
					workSet = mrio.getMediaRecords((Predicate) mmai.getSelector());
				}
				else if (mmai.getSelector() instanceof Vector) {
					addEvent("Vector found. Will pull MediaRecords based on mediaIDs.");
					workSet = mrio.getMediaRecords((Vector) mmai.getSelector());
				}

				addEvent("Query returned "+workSet.size()+" records");
				// if the object wasn't a match to what we expected or for some other reason
				// the workSet was not populated, bail
				if (workSet == null) {
					addEvent("Null workSet. Moving on to next task, if any.");
					continue;
				}

				// 2: Do the work on the selected records
				Iterator <MediaRecord> i = workSet.iterator();
				while (i.hasNext() && running) { // running is here to stop-fast on shutdown
					// a brief pause between records
					try { 
						Thread.sleep(sleepMsec); 
					}
					catch (InterruptedException ie) { 
						// if we terminated the sleep with an interrupt from shutdown(), quit the while loop
						if (! running)
							break;
					}
					// now get a record and process it
					MediaRecord mr = i.next();
					addEvent("Processing mediaID: "+mr.mediaID);
					if (mmai.process(mr)) {
						addEvent("Updating "+mr.mediaID);
						mrio.updateMediaRecord(mr);
					}
				}
			} // end of while (taskI.hasNext()
			addEvent("Full pass of all tasks completed");
		} // end of while(running)
		addEvent("Exiting");
	} // end of run() method


	/**
	 * Local convenience method to add an event to the server event log with this MediaMonitor's name prepended
	 * @param message The message to write into the log
	 */
	private void addEvent(String message) {
		dbel.addEvent("MediaMonitor "+this.name+": "+message);
	}


	private class TaskRecord {
		MediaMonitorActionI mmai;
		boolean repeat;
		TaskRecord(MediaMonitorActionI mmai, boolean repeat) {
			this.mmai = mmai;
			this.repeat = repeat;
		}
	}

} // end of MediaMonitor class
