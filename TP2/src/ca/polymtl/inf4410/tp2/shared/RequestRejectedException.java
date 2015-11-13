package ca.polymtl.inf4410.tp2.shared;

public class RequestRejectedException extends Exception{
      //Parameterless Constructor
      public RequestRejectedException() {
    	  super("Could not execute the request, the worker rejected the request");
      }

      //Constructor that accepts a message
      public RequestRejectedException(String message)
      {
         super(message);
      }
	
}
