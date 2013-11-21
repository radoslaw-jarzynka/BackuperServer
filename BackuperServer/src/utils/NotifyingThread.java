/**
 * created on 22:36:43 2 lis 2013 by Radoslaw Jarzynka
 * 
 * @author Radoslaw Jarzynka
 */
package utils;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class NotifyingThread extends Thread {
	  protected final Set<ThreadCompleteListener> listeners = new CopyOnWriteArraySet<ThreadCompleteListener>();
	  public final void addListener(final ThreadCompleteListener listener) {
	    listeners.add(listener);
	  }
	  public final void removeListener(final ThreadCompleteListener listener) {
	    listeners.remove(listener);
	  }
	  private final void notifyListeners() {
	    for (ThreadCompleteListener listener : listeners) {
	      listener.notifyOfThreadComplete(this);
	    }
	  }
	  @Override
	  public final void run() {
	    try {
	      doRun();
	    } finally {
	      notifyListeners();
	    }
	  }
	  public abstract void doRun();
	}
