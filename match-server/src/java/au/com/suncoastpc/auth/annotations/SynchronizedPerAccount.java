package au.com.suncoastpc.auth.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an API method as being synchronized per account.  Methods that are synchronized per account 
 * only permit a single thread per account at any given time.  This is to prevent issues with crosstalk 
 * during various operations that modify application state.  For instance, if one user starts synchronizing 
 * a batch of changes and another user tries to submit a change to an object that may be modified by the 
 * first person's batch of updates, the second user's request should not be allowed to proceed until after 
 * the first user's synchronization operation has finished.
 * 
 * As a general rule, any operation that modifies application state should be synchronized per account.
 * 
 * @author Adam
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface SynchronizedPerAccount {

}
