/*
 * Copyright (c) 2002-2009, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.portal.web.system;

import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.test.MokeHttpServletRequest;


/**
 * SystemJspBean Test Class
 *
 */
public class CacheJspBeanTest extends LuteceTestCase
{

    /**
     * Test of getManageCaches method, of class fr.paris.lutece.portal.web.system.SystemJspBean.
     */
    public void testGetManageCaches(  ) throws AccessDeniedException
    {
        System.out.println( "getManageCaches" );

        MokeHttpServletRequest request = new MokeHttpServletRequest(  );
        request.registerAdminUserWithRigth( new AdminUser(  ), CacheJspBean.RIGHT_CACHE_MANAGEMENT );

        CacheJspBean instance = new CacheJspBean(  );
        instance.init( request, CacheJspBean.RIGHT_CACHE_MANAGEMENT );
        instance.getManageCaches( request );
    }

    /**
     * Test of doResetCaches method, of class fr.paris.lutece.portal.web.system.SystemJspBean.
     */
    public void testDoResetCaches(  )
    {
        System.out.println( "doResetCaches" );

        MokeHttpServletRequest request = new MokeHttpServletRequest(  );
        request.registerAdminUserWithRigth( new AdminUser(  ), CacheJspBean.RIGHT_CACHE_MANAGEMENT );
        CacheJspBean.doResetCaches( request );
    }

    /**
     * Test of doReloadProperties method, of class fr.paris.lutece.portal.web.system.SystemJspBean.
     */
    public void testDoReloadProperties(  )
    {
        System.out.println( "doReloadProperties" );

        MokeHttpServletRequest request = new MokeHttpServletRequest(  );
        request.registerAdminUserWithRigth( new AdminUser(  ), CacheJspBean.RIGHT_CACHE_MANAGEMENT );

        CacheJspBean instance = new CacheJspBean(  );
        instance.doReloadProperties(  );
    }

}