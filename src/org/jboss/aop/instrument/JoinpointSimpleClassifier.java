/*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
package org.jboss.aop.instrument;

import java.util.Collection;
import java.util.Iterator;

import javassist.CtMember;
import javassist.NotFoundException;

import org.jboss.aop.Advisor;
import org.jboss.aop.AspectManager;
import org.jboss.aop.pointcut.Pointcut;
import org.jboss.logging.Logger;

/**
 * 
 *  we need to remove the wrong logging used by the class.
 * 
 * This joinpoint classifier is anaware of differences between <code>
 * PREPARED</code> and <code>WRAPPED</code> classifications.
 * It classifies a joinpoint either as something that must be instrumented
 * or something that mustn't, without caring about preparation.
 * Whenever a joinpoint must be instrumented, it is classified as <code>
 * WRAPPED</code>; by the other hand, whenever
 * it is should not be instrumented, it is classified as <code>NOT_INSTRUMENTED
 * </code>
 * 
 * @author Fitas Amine - fitas at nightlabs dot de
 * 
 ****/
public class JoinpointSimpleClassifier extends JoinpointClassifier
{
   
	
	   private static final Logger logger = Logger.getLogger(JoinpointSimpleClassifier.class);

	   
   /**
    * Classifies the execution of a joinpoint. The joinpoint being classified
    * is identified by <code>matcher</code>.
    * If the joinpoint is matched by one or more pointcuts, then
    * it is classified as <code>JoinpointClassification.WRAPPED</code>. Otherwise,
    * it is classified as <code>JoinpointClassification.NOT_INSTRUMENTED</code>.
    * @see org.jboss.aop.instrument.JoinpointClassifier#classifyJoinpoint(javassist.CtMember, org.jboss.aop.Advisor, org.jboss.aop.instrument.JoinpointClassifier.Matcher)
    */
   protected JoinpointClassification classifyJoinpoint(CtMember member, Advisor advisor, Matcher joinpointMatcher) throws NotFoundException
   {
      Collection pointcuts = advisor.getManager().getPointcuts().values();
      for (Iterator it = pointcuts.iterator(); it.hasNext(); )
      {
         Pointcut pointcut = (Pointcut) it.next();
         
         try
         {
            if (joinpointMatcher.matches(pointcut, advisor, member)) 
            {
               if (AspectManager.verbose)
               {
            	   logger.debug("[debug] " + member + " matches pointcut: " + pointcut.getExpr());
               }
               return JoinpointClassification.WRAPPED; 
            }
         }
         catch (RuntimeException e)
         {
            return handleError(e, member);
         }
      }
      if (AspectManager.verbose)
      {
    	  logger.debug("[debug] " + member + " matches no pointcuts");
      }
      return JoinpointClassification.NOT_INSTRUMENTED;
   }
   
   private JoinpointClassification handleError(RuntimeException e, CtMember member)
   {
      if (AspectManager.suppressTransformationErrors)
      {
         //An unused field may be of a type, or an unused method may have return-type/parameters that are not on the classpath
         //If supress transformationerrors=true, we should simply log that this member cannot be woven and continue.
         //Loadtime weaving with JRockit seems especially sensitive to this 
         NotFoundException nfe = null;
         Throwable cause = e.getCause();

         while (cause != null)
         {
            if (cause instanceof NotFoundException)
            {
               nfe = (NotFoundException)cause;
               break;
            }
            cause = cause.getCause();
         }
         
         if (nfe != null)
         {        	      
        	 logger.debug("The member " + member.getName() + " in " + member.getDeclaringClass().getName() + 
                  " uses the type " + nfe.getMessage() + " which cannot be found on the classpath. Weaving is therefore skipped for this particular member");
            if (AspectManager.verbose)
            {
               logger.debug(e.getCause());
            }
            return JoinpointClassification.NOT_INSTRUMENTED;
         }
      }
      throw e;
   }
}