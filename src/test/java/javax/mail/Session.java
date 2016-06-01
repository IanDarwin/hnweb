package javax.mail;

import javax.mail.Authenticator;
import javax.mail.Provider;
import javax.mail.Store;
import javax.mail.Transport;
import java.net.InetAddress;
import java.util.*;
import java.io.*;

public class Session {
	private Properties mProps = new Properties();
	private boolean debug;

	public Session(Properties p) {
		System.out.println("Session.init");
		this.mProps = p;
	}

  public static Session getInstance(Properties p, Authenticator auth){return new Session(p);}
  public static Session getInstance(Properties p){return new Session(p);}
  public static synchronized Session getDefaultInstance(Properties p, Authenticator auth){return new Session(p);}
  public static Session getDefaultInstance(Properties p){return new Session(p);}
  public synchronized void setDebug(boolean b){this.debug = b;}
  public synchronized boolean getDebug(){return debug;}
  public synchronized void setDebugOut(PrintStream p){return;}
  public synchronized PrintStream getDebugOut(){return null;}
  public synchronized Provider[] getProviders(){return new Provider[0];}
  public synchronized Provider getProvider(String s) throws NoSuchProviderException{throw new NoSuchProviderException();}
  public synchronized void setProvider(Provider p) throws NoSuchProviderException{throw new NoSuchProviderException();}
  public Store getStore() throws NoSuchProviderException{throw new NoSuchProviderException();}
  public Store getStore(String s) throws NoSuchProviderException{return null;}
  public Store getStore(URLName s) throws NoSuchProviderException{return null;}
  public Store getStore(Provider p) throws NoSuchProviderException{return null;}
  public Folder getFolder(URLName n) throws MessagingException{return null;}
  public Transport getTransport() throws NoSuchProviderException{return null;}
  public Transport getTransport(String t) throws NoSuchProviderException{return null;}
  public Transport getTransport(URLName t) throws NoSuchProviderException{return null;}
  public Transport getTransport(Provider t) throws NoSuchProviderException{return null;}
  public Transport getTransport(Address t) throws NoSuchProviderException{return null;}
  public void setPasswordAuthentication(URLName n, PasswordAuthentication pw){return;}
  public PasswordAuthentication getPasswordAuthentication(URLName n){return null;}
  public PasswordAuthentication requestPasswordAuthentication(InetAddress ad, int n, String s, String t, String u){return null;}
  public Properties getProperties(){return mProps;}
  public String getProperty(String propName){return null;}
  public synchronized void addProvider(Provider p){return;}
  public synchronized void setProtocolForAddress(String s, String t){return;}
}
