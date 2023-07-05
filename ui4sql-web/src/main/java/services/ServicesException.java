/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package services;



/**
  * This class reimplements the cause mechanism because it doesn't exist in 1.3.1
  * and we need it. Any code which should apply to the entire app's exceptions should
  * be placed here.
  */
public class ServicesException extends java.lang.Exception {

   Throwable cause;
   public ServicesException() {
     super();
   }

   public ServicesException(String message) {
     super(message);
   }

   public ServicesException(String message, Throwable cause ) {
     super(message);
     this.cause = cause;
   }

   public ServicesException( Throwable cause) {
     super();
     this.cause = cause;
   }
   public Throwable getCause (){
     return cause;
   }
   public void printStackTrace( java.io.PrintStream ps ){
     printStackTrace( new java.io.PrintWriter(ps, true ) );
   }
   public void printStackTrace( java.io.PrintWriter pw ){
     Throwable savedCause = cause;
     cause = null;
     super.printStackTrace( pw );
     cause = savedCause;
     for ( Throwable e = cause; e != null; ){
       if ( e instanceof ServicesException ){
         ServicesException te = (ServicesException)e;
         savedCause = te.cause;
         te.cause = null;
         pw.println( "caused by:" );
         te.printStackTrace( pw );
         te.cause = savedCause;
         e = te.cause;
       }else{
         e.printStackTrace( pw );
         e = null;
       }
     }
   }
}

