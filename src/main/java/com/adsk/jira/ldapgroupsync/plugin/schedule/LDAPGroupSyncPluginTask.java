/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.adsk.jira.ldapgroupsync.plugin.schedule;

import com.adsk.jira.ldapgroupsync.plugin.api.LDAPGroupSyncUtil;
import com.adsk.jira.ldapgroupsync.plugin.model.LdapGroupSyncMapBean;
import com.adsk.jira.ldapgroupsync.plugin.model.MessageBean;
import com.atlassian.sal.api.scheduling.PluginJob;
import java.util.Date;
import java.util.Map;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import org.apache.log4j.Logger;

/**
 *
 * @author prasadve
 */
public class LDAPGroupSyncPluginTask implements PluginJob {
    private static final Logger logger = Logger.getLogger(LDAPGroupSyncPluginTask.class);
    
    public void execute(Map<String, Object> jobDataMap) {
        
        final LDAPGroupSyncPluginSchedule sch = (LDAPGroupSyncPluginScheduleImpl) 
                jobDataMap.get(LDAPGroupSyncPluginScheduleImpl.KEY);
        
        assert sch != null;
        
        sch.setLastRun(new Date());
        
        /**
         * JRASERVER-29896
         */
        final Thread currentThread = Thread.currentThread();
        final ClassLoader origCCL = currentThread.getContextClassLoader();        
        currentThread.setContextClassLoader(LDAPGroupSyncPluginTask.class.getClass().getClassLoader());
        
        LDAPGroupSyncUtil syncUtil = sch.getLDAPGroupSyncUtil();
        LdapContext ctx = null;
        
        try {               
            ctx = syncUtil.getLdapContext();
                        
            for(LdapGroupSyncMapBean bean : sch.getLdapGroupSyncAOMgr().getSupportedGroupsMapProperties()) {
                MessageBean message = syncUtil.sync(ctx, bean.getLdapGroup(), bean.getJiraGroup());
                logger.debug(message.getMessage());
            }
            
        } finally {
            try {
                if(ctx != null) ctx.close();
            } catch (NamingException e) {
                logger.error(e);
            }
            
            currentThread.setContextClassLoader(origCCL); //JRASERVER-29896
        }
    }
    
}