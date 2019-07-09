package com.ecm.db.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import javax.naming.ldap.*;
import javax.naming.directory.*;
import javax.naming.*;

import com.ecm.db.model.ECMMailManager;
import com.ecm.db.transport.TADPrincipal;
import com.ecm.db.transport.TAdminEmailSet;

public class ADManager
{
	//Attribute names
    private static final String AD_ATTR_NAME_SAM_ACCOUNT_NAME = "SAMAccountName";
    private static final String AD_ATTR_NAME_DISPLAY_NAME = "displayName";
    private static final String AD_ATTR_NAME_GROUP_NAME = "name";
    private static final String AD_ATTR_NAME_CN = "cn";
    
    public static final String DISTINGUISHED_NAME = "distinguishedName";
    public static final String MEMBER = "member";
    public static final String MEMBER_OF = "memberOf";
    public static final String SEARCH_BY_SAM_ACCOUNT_NAME = "(SAMAccountName={0})";
    public static final String SEARCH_GROUP_BY_GROUP_CN = "(&(objectCategory=group)(cn={0}))";
    
	private String cnUserBase;
	private String cnGroupBase;
	private String adminName;
    private String adminPassword;
    private String ldapHost;
    private int ldapPort;
	
    public static ADManager getInstance(String cnuserbase, String cngroupbase, String adminname, 
    		String adminpassword, String ldaphost, int ldapport) {
    	return new ADManager(cnuserbase, cngroupbase, adminname, adminpassword, ldaphost, ldapport);
    }
    
    private ADManager(String cnuserbase, String cngroupbase, String adminname, 
    		String adminpassword, String ldaphost, int ldapport) {
    	this.cnUserBase = cnuserbase;
    	this.cnGroupBase = cngroupbase;
    	this.adminName = adminname;
    	this.adminPassword = adminpassword;
    	this.ldapHost = ldaphost;
    	this.ldapPort = ldapport;
    }
    
    private String getDistinguishedName(String baseDN, String simpleName) {
    	return "CN=" + simpleName + "," + baseDN;
    }
    
    public void addUserToGroup(String userName, String groupName) throws Exception
    {
    	Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
     
        //set security credentials, note using simple cleartext authentication
        env.put(Context.SECURITY_AUTHENTICATION,"simple");
        env.put(Context.SECURITY_PRINCIPAL, adminName);//getDistinguishedName(cnUserBase, adminName));
        env.put(Context.SECURITY_CREDENTIALS,adminPassword);
                    
        //connect to my domain controller
        env.put(Context.PROVIDER_URL, "ldap://" + ldapHost + ":" + ldapPort);
                    
        try {
        	// Create the initial directory context
        	LdapContext ctx = new InitialLdapContext(env,null);
             
		    ModificationItem member[] = new ModificationItem[1];
		    member[0]= new ModificationItem(DirContext.ADD_ATTRIBUTE, 
		    		new BasicAttribute("member", getDistinguishedName(cnUserBase, userName))); 
		
		    ctx.modifyAttributes(getDistinguishedName(cnGroupBase, groupName),member);
		    System.out.println("Added user to group: " + groupName);
		
		    ctx.close();
		    if(!(sendEmailAddedUser(userName,groupName)))
			{
					//New method to send email later
			}
		    
        } catch (NamingException e) {
            System.err.println("Problem adding user to group: " + e);
        }
    }

private Boolean sendEmailAddedUser(String userName, String groupName){
		
		try {
			ECMMailManager em=new ECMMailManager();
			TAdminEmailSet taes=new TAdminEmailSet();
			taes.name=userName;
			taes.adGroup=groupName;
			em.sendEmail("ECMRoleM", taes, "Added");
			
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		
	}
	
    public void removeUserFromGroup(String userName, String groupName) throws Exception
    {
    	Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
     
        //set security credentials, note using simple cleartext authentication
        env.put(Context.SECURITY_AUTHENTICATION,"simple");
        env.put(Context.SECURITY_PRINCIPAL, adminName);//getDistinguishedName(cnUserBase, adminName));
        env.put(Context.SECURITY_CREDENTIALS,adminPassword);
                    
        //connect to my domain controller
        env.put(Context.PROVIDER_URL, "ldap://" + ldapHost + ":" + ldapPort);
                    
        try {
        	// Create the initial directory context
        	LdapContext ctx = new InitialLdapContext(env,null);
             
		    ModificationItem member[] = new ModificationItem[1];
		    member[0]= new ModificationItem(DirContext.REMOVE_ATTRIBUTE, 
		    		new BasicAttribute("member", getDistinguishedName(cnUserBase, userName))); 
		
		    ctx.modifyAttributes(getDistinguishedName(cnGroupBase, groupName),member);
		    System.out.println("Added user to group: " + groupName);
		
		    ctx.close();
		    if(!(sendEmail(userName,groupName)))
			{
					//New method to send email later
			}
		    
        } catch (NamingException e) {
            System.err.println("Problem adding user to group: " + e);
        }
    }

 private Boolean sendEmail(String userName, String groupName){
		
		try {
			ECMMailManager em=new ECMMailManager();
			TAdminEmailSet taes=new TAdminEmailSet();
			taes.name=userName;
			taes.adGroup=groupName;
			em.sendEmail("ECMRoleM", taes, "Removed");
			
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		
	}
    
    public ArrayList<TADPrincipal> getGroupMembers(String groupName) throws Exception
    {
    	Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
     
        //set security credentials, note using simple cleartext authentication
        env.put(Context.SECURITY_AUTHENTICATION,"simple");
        env.put(Context.SECURITY_PRINCIPAL, adminName);//getDistinguishedName(cnUserBase, adminName));
        env.put(Context.SECURITY_CREDENTIALS,adminPassword);
                    
        //connect to my domain controller
        env.put(Context.PROVIDER_URL, "ldap://" + ldapHost + ":" + ldapPort);
                  
        ArrayList<TADPrincipal> userList = new ArrayList<TADPrincipal>();
        
        try {
        	// Create the initial directory context
        	LdapContext ctx = new InitialLdapContext(env,null);
        	String searchFilter = "(&(objectClass=group)(name=" + groupName + "))";
        	
        	SearchControls controls = new SearchControls();
    	    controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    	    NamingEnumeration<SearchResult> results = ctx.search(cnUserBase, searchFilter, controls);

            SearchResult searchResult = null;
            while(results.hasMoreElements()) {
                 searchResult = (SearchResult) results.nextElement();
                 Attributes attributes = searchResult.getAttributes();	 
                /* TADPrincipal tadp = new TADPrincipal();
		          tadp.login = getAttribute(attributes,AD_ATTR_NAME_SAM_ACCOUNT_NAME);
		          tadp.name = getAttribute(attributes,"cn");
		          tadp.type = "GROUP"; */
		          
		          Attribute attr = attributes.get("member");
		    	  if(attr != null) {
		    	     for (int i=0; i < attr.size();i++)
		    	     {
		    	    	 TADPrincipal tadp = getUser(ctx, (String) attr.get(i));
		    	    	 if(tadp != null)
		             		userList.add(tadp);
		    	     }
		    	  }
		          //userList.add(tadp);
            }
            /*
    	    NamingEnumeration results = ctx.search(cnGroupBase, searchFilter, controls);
        	
    	    while (results.hasMore())
    	    {
	    	     SearchResult searchResult = (SearchResult) results.next();
	    	     Attributes attributes = searchResult.getAttributes();
	    	     Attribute attr = attributes.get("uniqueMember");
	    	     
	    	     for (int i=0; i < attr.size();i++)
	    	     {
	    	    	 TADPrincipal tadp = getUser(ctx, (String) attr.get(i));
	    	    	 if(tadp != null)
	             		userList.add(tadp);
	    	     }
    	    } */
    	    
		    System.out.println("Retrieved users of group: " + groupName);
		
		    ctx.close();
		    
        } catch (NamingException e) {
            System.err.println("Problem adding user to group: " + e);
        }
        return userList;
    }
    
    public ArrayList<TADPrincipal> searchUsers(String userName) throws Exception
    {
    	Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
     
        //set security credentials, note using simple cleartext authentication
        env.put(Context.SECURITY_AUTHENTICATION,"simple");
        env.put(Context.SECURITY_PRINCIPAL, adminName);//getDistinguishedName(cnUserBase, adminName));
        env.put(Context.SECURITY_CREDENTIALS,adminPassword);
                    
        //connect to my domain controller
        env.put(Context.PROVIDER_URL, "ldap://" + ldapHost + ":" + ldapPort);
                  
        ArrayList<TADPrincipal> userList = new ArrayList<TADPrincipal>();
        
        try {
        	// Create the initial directory context
        	LdapContext ctx = new InitialLdapContext(env,null);
             
        	 String searchFilter = "(&(objectClass=user)(|(sAMAccountName=*" + userName + "*)"
        	 		+ "(displayName=*" + userName + "*) (cn=*" + userName + "*)))";

             SearchControls searchControls = new SearchControls();
             searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

             NamingEnumeration<SearchResult> results = ctx.search(cnUserBase, searchFilter, searchControls);

             SearchResult searchResult = null;
             while(results.hasMoreElements()) {
                  searchResult = (SearchResult) results.nextElement();
                  Attributes attributes = searchResult.getAttributes();	 
                  TADPrincipal tadp = new TADPrincipal();
		          tadp.login = getAttribute(attributes,AD_ATTR_NAME_SAM_ACCOUNT_NAME);
		          tadp.name = getAttribute(attributes,AD_ATTR_NAME_DISPLAY_NAME);
		          tadp.type = "USER";
		          
		          String username = tadp.name;
		          
		          if(username != null && username.length() > 0)
		        	  userList.add(tadp);
             }
		    System.out.println("Retrieved users of name: " + userName);
		
		    ctx.close();
		    
        } catch (NamingException e) {
            System.err.println("Problem searching users: " + e);
        }
        return userList;
    }
    
    public ArrayList<TADPrincipal> searchGroups(String groupName) throws Exception
    {
    	Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
     
        //set security credentials, note using simple cleartext authentication
        env.put(Context.SECURITY_AUTHENTICATION,"simple");
        env.put(Context.SECURITY_PRINCIPAL, adminName);//getDistinguishedName(cnUserBase, adminName));
        env.put(Context.SECURITY_CREDENTIALS,adminPassword);
                    
        //connect to my domain controller
        env.put(Context.PROVIDER_URL, "ldap://" + ldapHost + ":" + ldapPort);
                  
        ArrayList<TADPrincipal> userList = new ArrayList<TADPrincipal>();
        
        try {
        	// Create the initial directory context
        	LdapContext ctx = new InitialLdapContext(env,null);
        	String searchFilter = "(&(objectClass=group)(name=*" + groupName + "*))";
        	
        	SearchControls controls = new SearchControls();
    	    controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    	    NamingEnumeration<SearchResult> results = ctx.search(cnUserBase, searchFilter, controls);

            SearchResult searchResult = null;
            while(results.hasMoreElements()) {
                 searchResult = (SearchResult) results.nextElement();
                 Attributes attributes = searchResult.getAttributes();	 
                 TADPrincipal tadp = new TADPrincipal();
		          tadp.login = getAttribute(attributes,AD_ATTR_NAME_SAM_ACCOUNT_NAME);
		          tadp.name = getAttribute(attributes,AD_ATTR_NAME_GROUP_NAME);
		          tadp.type = "GROUP"; 
		          
		          String groupname = tadp.name;
		          
		          if(groupname != null && groupname.length() > 0)
		        	  userList.add(tadp);
            }
    	    
		    System.out.println("Retrieved groups starting with: " + groupName);
		
		    ctx.close();
		    
        } catch (NamingException e) {
            System.err.println("Problem searching groups: " + e);
        }
        return userList;
    }
    
    private String getAttribute(Attributes attributes, String attid) {
    	String attribute = "";
    	try {
    		attribute = attributes.get(attid).get().toString();
    	} catch (Exception e) {}
    	return attribute;
    }
    
    private TADPrincipal getUser(LdapContext ctx, String userName)
    {
        try {
             
        	 String searchFilter = "(&(objectClass=user)(|(sAMAccountName=" + userName + ")"
        	 		+ "(distinguishedName=" + userName + ")))";

             SearchControls searchControls = new SearchControls();
             searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

             NamingEnumeration<SearchResult> results = ctx.search(cnUserBase, searchFilter, searchControls);

             SearchResult searchResult = null;
             if(results.hasMoreElements()) {
                  searchResult = (SearchResult) results.nextElement();
                  Attributes attributes = searchResult.getAttributes();	 
                  TADPrincipal tadp = new TADPrincipal();
		          tadp.login = getAttribute(attributes,AD_ATTR_NAME_SAM_ACCOUNT_NAME);
		          tadp.name = getAttribute(attributes,AD_ATTR_NAME_DISPLAY_NAME);
		          tadp.type = "USER";
		          
		          return tadp;
             }
		    System.out.println("Retrieved users of name: " + userName);
		    
        } catch (NamingException e) {
            System.err.println("Problem searching users: " + e);
        }
        return null;
    }
    
    public static boolean userMemberOf(DirContext ctx, String searchBase, HashMap processedUserGroups, HashMap unProcessedUserGroups, String groupCN, String groupDistinguishedName) throws NamingException {
        HashMap newUnProcessedGroups = new HashMap();
        for (Iterator entry = unProcessedUserGroups.keySet().iterator(); entry.hasNext();) {
            String  unprocessedGroupDistinguishedName = (String) entry.next();
            String unprocessedGroupCN = (String)unProcessedUserGroups.get(unprocessedGroupDistinguishedName);
            if ( processedUserGroups.get(unprocessedGroupDistinguishedName) != null) {
                //Log.info("Found  : " + unprocessedGroupDistinguishedName +" in processedGroups. skipping further processing of it..." );
                // We already traversed this.
                continue;
            }
            if (isSame (groupCN, unprocessedGroupCN) && isSame (groupDistinguishedName, unprocessedGroupDistinguishedName)) {
               // Log.info("Found Match DistinguishedName : " + unprocessedGroupDistinguishedName +", CN : " + unprocessedGroupCN );
                return true;
            }
        }

        for (Iterator entry = unProcessedUserGroups.keySet().iterator(); entry.hasNext();) {
            String  unprocessedGroupDistinguishedName = (String) entry.next();
            String unprocessedGroupCN = (String)unProcessedUserGroups.get(unprocessedGroupDistinguishedName);

            processedUserGroups.put(unprocessedGroupDistinguishedName, unprocessedGroupCN);

            // Fetch Groups in unprocessedGroupCN and put them in newUnProcessedGroups
            NamingEnumeration ns = executeSearch(ctx, SearchControls.SUBTREE_SCOPE, searchBase,
                    MessageFormat.format( SEARCH_GROUP_BY_GROUP_CN, new Object[] {unprocessedGroupCN}),
                    new String[] {AD_ATTR_NAME_CN, DISTINGUISHED_NAME, MEMBER_OF});

            // Loop through the search results
            while (ns.hasMoreElements()) {
                SearchResult sr = (SearchResult) ns.next();

                // Make sure we're looking at correct distinguishedName, because we're querying by CN
                String userDistinguishedName = sr.getAttributes().get(DISTINGUISHED_NAME).get().toString();
                if (!isSame(unprocessedGroupDistinguishedName, userDistinguishedName)) {
                    //Log.info("Processing CN : " + unprocessedGroupCN + ", DN : " + unprocessedGroupDistinguishedName +", Got DN : " + userDistinguishedName +", Ignoring...");
                    continue;
                }

                //Log.info("Processing for memberOf CN : " + unprocessedGroupCN + ", DN : " + unprocessedGroupDistinguishedName);
                // Look for and process memberOf
                Attribute memberOf = sr.getAttributes().get(MEMBER_OF);
                if (memberOf != null) {
                    for ( Enumeration e1 = memberOf.getAll() ; e1.hasMoreElements() ; ) {
                        String unprocessedChildGroupDN = e1.nextElement().toString();
                        String unprocessedChildGroupCN = getCN(unprocessedChildGroupDN);
                        //Log.info("Adding to List of un-processed groups : " + unprocessedChildGroupDN +", CN : " + unprocessedChildGroupCN);
                        newUnProcessedGroups.put(unprocessedChildGroupDN, unprocessedChildGroupCN);
                    }
                }
            }
        }
        if (newUnProcessedGroups.size() == 0) {
            //Log.info("newUnProcessedGroups.size() is 0. returning false...");
            return false;
        }

        //  process unProcessedUserGroups
        return userMemberOf(ctx, searchBase, processedUserGroups, newUnProcessedGroups, groupCN, groupDistinguishedName);
    }

    private static NamingEnumeration executeSearch(DirContext ctx, int searchScope,  String searchBase, String searchFilter, String[] attributes) throws NamingException {
        // Create the search controls
        SearchControls searchCtls = new SearchControls();

        // Specify the attributes to return
        if (attributes != null) {
            searchCtls.setReturningAttributes(attributes);
        }

        // Specify the search scope
        searchCtls.setSearchScope(searchScope);

        // Search for objects using the filter
        NamingEnumeration result = ctx.search(searchBase, searchFilter,searchCtls);
        return result;
    }
    
    private static SearchResult executeSearchSingleResult(DirContext ctx, int searchScope,  String searchBase, String searchFilter, String[] attributes) throws NamingException {
        NamingEnumeration result = executeSearch(ctx, searchScope,  searchBase, searchFilter, attributes);

        SearchResult sr = null;
        // Loop through the search results
        while (result.hasMoreElements()) {
            sr = (SearchResult) result.next();
            break;
        }
        return sr;
    }
    
    public static String getCN(String cnName) {
        if (cnName != null && cnName.toUpperCase().startsWith("CN=")) {
            cnName = cnName.substring(3);
        }
        int position = cnName.indexOf(',');
        if (position == -1) {
            return cnName;
        } else {
            return cnName.substring(0, position);
        }
    }
    public static boolean isSame(String target, String candidate) {
        if (target != null && target.equalsIgnoreCase(candidate)) {
            return true;
        }
        return false;
    }
}
