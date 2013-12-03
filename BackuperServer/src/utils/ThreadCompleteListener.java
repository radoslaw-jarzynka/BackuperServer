/**
 * created on 22:35:11 2 lis 2013 by Radoslaw Jarzynka
 * 
 * @author Radoslaw Jarzynka
 */
package utils;
// interfejs listenera sluchajacy czy jakis watek klasy notifyingthread zakonczyl swoje dzialanie
public interface ThreadCompleteListener {
    void notifyOfThreadComplete(final Thread thread);
}