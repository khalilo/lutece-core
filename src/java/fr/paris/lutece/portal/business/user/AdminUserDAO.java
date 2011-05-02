/*
 * Copyright (c) 2002-2010, Mairie de Paris
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
package fr.paris.lutece.portal.business.user;

import fr.paris.lutece.portal.business.rbac.AdminRole;
import fr.paris.lutece.portal.business.right.Right;
import fr.paris.lutece.portal.business.user.authentication.LuteceDefaultAdminUser;
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * This class porvides Data Access methods for AdminUser objects
 */
public class AdminUserDAO implements IAdminUserDAO
{
    // Constants
    private static final String CONSTANT_AND_STATUS = " AND status = ?";
    private static final String CONSTANT_AND_USER_LEVEL = " AND level_user = ?";
    private static final String CONSTANT_ORDER_BY_LAST_NAME = " ORDER BY last_name ";
    private static final String CONSTANT_PERCENT = "%";
    private static final String SQL_QUERY_NEWPK = "SELECT max( id_user ) FROM core_admin_user ";
    private static final String SQL_QUERY_INSERT = "INSERT INTO core_admin_user ( id_user , access_code, last_name , first_name, email, status, locale, level_user, accessibility_mode )  VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_SELECTALL = "SELECT id_user , access_code, last_name , first_name, email, status, locale, level_user, accessibility_mode FROM core_admin_user ORDER BY last_name ";
    private static final String SQL_QUERY_SELECT_USER_FROM_USER_ID = "SELECT id_user , access_code, last_name , first_name, email, status, password, locale, level_user, reset_password, accessibility_mode FROM core_admin_user  WHERE id_user = ? ORDER BY last_name";
    private static final String SQL_QUERY_SELECT_USER_FROM_ACCESS_CODE = "SELECT id_user, access_code, last_name, first_name, email, status, locale, level_user, reset_password, accessibility_mode FROM core_admin_user  WHERE access_code = ? ";
    private static final String SQL_QUERY_SELECT_RIGHTS_FROM_USER_ID = " SELECT a.id_right , a.name, a.admin_url , a.description , a.plugin_name, a.id_feature_group, a.icon_url, a.level_right, a.documentation_url, a.id_order " +
        " FROM core_admin_right a , core_user_right b " + " WHERE a.id_right = b.id_right " + " AND b.id_user = ? " +
        " ORDER BY a.id_order ASC, a.id_right ASC ";
    private static final String SQL_QUERY_UPDATE = "UPDATE core_admin_user SET access_code = ? , last_name = ? , first_name = ?, email = ?, status = ?, locale = ?, reset_password = ?, accessibility_mode = ? WHERE id_user = ?  ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM core_admin_user WHERE id_user = ? ";
    private static final String SQL_QUERY_INSERT_USER_RIGHT = "INSERT INTO core_user_right ( id_right, id_user )  VALUES ( ? , ? ) ";
    private static final String SQL_QUERY_DELETE_ALL_USER_RIGHTS = "DELETE FROM core_user_right WHERE id_user = ? ";
    private static final String SQL_QUERY_SELECT_ROLES_FROM_USER_ID = " SELECT a.role_key , a.role_description " +
        " FROM core_admin_role a , core_user_role b " + " WHERE a.role_key = b.role_key " +
        " AND b.id_user = ?  ORDER BY a.role_key ";
    private static final String SQL_QUERY_INSERT_USER_ROLE = " INSERT INTO core_user_role ( role_key, id_user )  VALUES ( ? , ? ) ";
    private static final String SQL_QUERY_DELETE_ALL_USER_ROLES = " DELETE FROM core_user_role WHERE id_user = ? ";
    private static final String SQL_CHECK_ROLE_ATTRIBUTED = " SELECT id_user FROM core_user_role WHERE role_key = ?";
    private static final String SQL_CHECK_ACCESS_CODE_IN_USE = " SELECT id_user FROM core_admin_user WHERE access_code = ?";
    private static final String SQL_CHECK_EMAIL_IN_USE = " SELECT id_user FROM core_admin_user WHERE email = ?";
    private static final String SQL_QUERY_INSERT_DEFAULT_USER = " INSERT INTO core_admin_user ( id_user, access_code, last_name, first_name, email, status, password, locale, level_user, accessibility_mode )  VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_UPDATE_DEFAULT_USER = " UPDATE core_admin_user SET access_code = ?, last_name = ?, first_name = ?, email = ?, status = ?, password = ?, locale = ?, reset_password = ?, accessibility_mode = ? WHERE id_user = ?  ";
    private static final String SQL_QUERY_SELECT_USERS_ID_BY_ROLES = " SELECT a.id_user , a.access_code, a.last_name , a.first_name, a.email, a.status, a.locale, a.accessibility_mode " +
        " FROM core_admin_user a, core_user_role b WHERE a.id_user = b.id_user AND b.role_key = ? ";
    private static final String SQL_QUERY_SELECT_USER_RIGHTS_OWN = " SELECT DISTINCT b.id_right FROM core_admin_right a , core_user_right b WHERE b.id_user = ? and a.id_right = b.id_right and a.level_right >= ?";
    private static final String SQL_QUERY_SELECT_USER_RIGHTS_DELEGATED = " SELECT DISTINCT b.id_right FROM core_admin_right a , core_user_right b WHERE b.id_user = ? and a.id_right = b.id_right and a.level_right < ?";
    private static final String SQL_QUERY_DELETE_USER_RIGHTS = " DELETE FROM core_user_right WHERE id_user = ? and id_right = ?";
    private static final String SQL_QUERY_SELECT_USERS_BY_LEVEL = " SELECT a.id_user, a.access_code, a.last_name, a.first_name, a.email, a.status, a.locale, a.accessibility_mode " +
        " FROM core_admin_user a WHERE a.level_user = ? ";
    private static final String SQL_QUERY_UPDATE_USERS_ROLE = "UPDATE core_user_role SET role_key = ? WHERE role_key = ?";
    private static final String SQL_QUERY_SELECT_USER_ROLE = " SELECT id_user FROM core_user_role WHERE id_user = ? AND role_key = ? ";
    private static final String SQL_QUERY_DELETE_ROLE_FOR_USER = " DELETE FROM core_user_role WHERE id_user = ? AND role_key = ? ";
    private static final String SQL_QUERY_SELECT_USER_FROM_SEARCH = " SELECT id_user, access_code, last_name, first_name, email, status, locale, level_user, accessibility_mode " +
        " FROM core_admin_user WHERE access_code LIKE ? AND last_name LIKE ? AND first_name LIKE ? AND email LIKE ? ";
    private static final String SQL_QUERY_SELECT_USERS_BY_RIGHT = " SELECT  u.id_user , u.access_code, u.last_name , u.first_name, u.email, u.status, u.locale, u.level_user, u.accessibility_mode " +
        " FROM core_admin_user u INNER JOIN core_user_right r ON u.id_user = r.id_user WHERE r.id_right = ? ";
    private static final String SQL_QUERY_SELECT_USER_RIGHT = " SELECT id_user FROM core_user_right WHERE id_user = ? AND id_right = ? ";

    /**
     * @param nUserId th user id
     * @return user
     */
    public AdminUser load( int nUserId )
    {
        AdminUser user = null;
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_USER_FROM_USER_ID );
        daoUtil.setInt( 1, nUserId );
        daoUtil.executeQuery(  );

        if ( daoUtil.next(  ) )
        {
            user = new AdminUser(  );
            user.setUserId( daoUtil.getInt( 1 ) );
            user.setAccessCode( daoUtil.getString( 2 ) );
            user.setLastName( daoUtil.getString( 3 ) );
            user.setFirstName( daoUtil.getString( 4 ) );
            user.setEmail( daoUtil.getString( 5 ) );
            user.setStatus( daoUtil.getInt( 6 ) );
            user.setLocale( new Locale( daoUtil.getString( 8 ) ) );
            user.setUserLevel( daoUtil.getInt( 9 ) );
            user.setPasswordReset( daoUtil.getBoolean( 10 ) );
            user.setAccessibilityMode( daoUtil.getBoolean( 11 ) );
        }

        daoUtil.free(  );

        return user;
    }

    /**
     * @param strUserAccessCode the login
     * @return user The admin User
     */
    public AdminUser selectUserByAccessCode( String strUserAccessCode )
    {
        AdminUser user = null;
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_USER_FROM_ACCESS_CODE );
        daoUtil.setString( 1, strUserAccessCode );
        daoUtil.executeQuery(  );

        if ( daoUtil.next(  ) )
        {
            user = new AdminUser(  );
            user.setUserId( daoUtil.getInt( 1 ) );
            user.setAccessCode( daoUtil.getString( 2 ) );
            user.setLastName( daoUtil.getString( 3 ) );
            user.setFirstName( daoUtil.getString( 4 ) );
            user.setEmail( daoUtil.getString( 5 ) );
            user.setStatus( daoUtil.getInt( 6 ) );
            user.setLocale( new Locale( daoUtil.getString( 7 ) ) );
            user.setUserLevel( daoUtil.getInt( 8 ) );
            user.setPasswordReset( daoUtil.getBoolean( 9 ) );
            user.setAccessibilityMode( daoUtil.getBoolean( 10 ) );
        }

        daoUtil.free(  );

        return user;
    }

    /**
     * Get a list of all admin users
     * @return userList The list
     */
    public Collection<AdminUser> selectUserList(  )
    {
        Collection<AdminUser> userList = new ArrayList<AdminUser>(  );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            AdminUser user = new AdminUser(  );
            user.setUserId( daoUtil.getInt( 1 ) );
            user.setAccessCode( daoUtil.getString( 2 ) );
            user.setLastName( daoUtil.getString( 3 ) );
            user.setFirstName( daoUtil.getString( 4 ) );
            user.setEmail( daoUtil.getString( 5 ) );
            user.setStatus( daoUtil.getInt( 6 ) );
            user.setLocale( new Locale( daoUtil.getString( 7 ) ) );
            user.setUserLevel( daoUtil.getInt( 8 ) );
            user.setAccessibilityMode( daoUtil.getBoolean( 9 ) );
            userList.add( user );
        }

        daoUtil.free(  );

        return userList;
    }

    /**
     * Generates a new primary key
         * @return nKey
     */
    public int newPrimaryKey(  )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_NEWPK );
        daoUtil.executeQuery(  );

        int nKey;

        if ( !daoUtil.next(  ) )
        {
            // if the table is empty
            nKey = 1;
        }

        nKey = daoUtil.getInt( 1 ) + 1;

        daoUtil.free(  );

        return nKey;
    }

    /**
     * Insert a new record in the table.
     * @param user The AdminUser
     */
    public synchronized void insert( AdminUser user )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT );

        user.setUserId( newPrimaryKey(  ) );
        daoUtil.setInt( 1, user.getUserId(  ) );
        daoUtil.setString( 2, user.getAccessCode(  ) );
        daoUtil.setString( 3, user.getLastName(  ) );
        daoUtil.setString( 4, user.getFirstName(  ) );
        daoUtil.setString( 5, user.getEmail(  ) );
        daoUtil.setInt( 6, user.getStatus(  ) );
        daoUtil.setString( 7, user.getLocale(  ).toString(  ) );
        daoUtil.setInt( 8, user.getUserLevel(  ) );
        daoUtil.setBoolean( 9, user.getAccessibilityMode(  ) );

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * Store an admin user
      * @param user The AdminUser
     */
    public void store( AdminUser user )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE );

        daoUtil.setString( 1, user.getAccessCode(  ) );
        daoUtil.setString( 2, user.getLastName(  ) );
        daoUtil.setString( 3, user.getFirstName(  ) );
        daoUtil.setString( 4, user.getEmail(  ) );
        daoUtil.setInt( 5, user.getStatus(  ) );
        daoUtil.setString( 6, user.getLocale(  ).toString(  ) );
        daoUtil.setBoolean( 7, user.isPasswordReset(  ) );
        daoUtil.setBoolean( 8, user.getAccessibilityMode(  ) );

        daoUtil.setInt( 9, user.getUserId(  ) );

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * Delete a admin user
     * @param nUserId the user id
     */
    public void delete( int nUserId )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE );
        daoUtil.setInt( 1, nUserId );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * Get the right list associated to a given user id
     * @param nUserId the id of the user to retrieve rights
     * @return the right list as a collection of strings
     */
    public Map<String, Right> selectRightsListForUser( int nUserId )
    {
        Map<String, Right> rightsMap = new HashMap<String, Right>(  );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_RIGHTS_FROM_USER_ID );
        daoUtil.setInt( 1, nUserId );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            Right right = new Right(  );
            right.setId( daoUtil.getString( 1 ) );
            right.setNameKey( daoUtil.getString( 2 ) );
            right.setUrl( daoUtil.getString( 3 ) );
            right.setDescriptionKey( daoUtil.getString( 4 ) );
            right.setPluginName( daoUtil.getString( 5 ) );
            right.setFeatureGroup( daoUtil.getString( 6 ) );
            right.setIconUrl( daoUtil.getString( 7 ) );
            right.setLevel( daoUtil.getInt( 8 ) );
            right.setDocumentationUrl( daoUtil.getString( 9 ) );
            right.setOrder( daoUtil.getInt( 10 ) );
            rightsMap.put( right.getId(  ), right );
        }

        daoUtil.free(  );

        return rightsMap;
    }

    /**
     * Insert a right for a user
     * @param nUserId the user id
     * @param strRightId the right id
     */
    public void insertRightsListForUser( int nUserId, String strRightId )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_USER_RIGHT );
        daoUtil.setString( 1, strRightId );
        daoUtil.setInt( 2, nUserId );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * Delete all rights for an user
     * @param nUserId the user id
     */
    public void deleteAllRightsForUser( int nUserId )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_ALL_USER_RIGHTS );
        daoUtil.setInt( 1, nUserId );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * Get the role list associated to a given user id
     * @param nUserId the id of the user to retrieve roles
     * @return the role list
     */
    public Map<String, AdminRole> selectRolesListForUser( int nUserId )
    {
        Map<String, AdminRole> rolesMap = new HashMap<String, AdminRole>(  );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ROLES_FROM_USER_ID );
        daoUtil.setInt( 1, nUserId );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            AdminRole role = new AdminRole(  );
            role.setKey( daoUtil.getString( 1 ) );
            role.setDescription( daoUtil.getString( 2 ) );

            rolesMap.put( role.getKey(  ), role );
        }

        daoUtil.free(  );

        return rolesMap;
    }

    /**
     * Add a role to an user
     * @param nUserId the user id
     * @param strRoleKey the key role
     */
    public void insertRolesListForUser( int nUserId, String strRoleKey )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_USER_ROLE );
        daoUtil.setString( 1, strRoleKey );
        daoUtil.setInt( 2, nUserId );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * Remove all roles from an user
     * @param nUserId the user id
     */
    public void deleteAllRolesForUser( int nUserId )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_ALL_USER_ROLES );
        daoUtil.setInt( 1, nUserId );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * Checks wether the role is in use or not
     * @param strRoleKey the role key to check
     * @return true if the role is attributed, false otherwise
     */
    public boolean checkRoleAttributed( String strRoleKey )
    {
        boolean bInUse = false;

        DAOUtil daoUtil = new DAOUtil( SQL_CHECK_ROLE_ATTRIBUTED );
        daoUtil.setString( 1, strRoleKey );
        daoUtil.executeQuery(  );

        if ( daoUtil.next(  ) )
        {
            bInUse = true;
        }

        daoUtil.free(  );

        return bInUse;
    }

    /**
     * Check weather the access code already exists or not
     * @param strAccessCode The access code
     * @return user ID if the access code is already used by another user, -1 otherwise
     */
    public int checkAccessCodeAlreadyInUse( String strAccessCode )
    {
        int nIdUser = -1;
        DAOUtil daoUtil = new DAOUtil( SQL_CHECK_ACCESS_CODE_IN_USE );
        daoUtil.setString( 1, strAccessCode );
        daoUtil.executeQuery(  );

        if ( daoUtil.next(  ) )
        {
            nIdUser = daoUtil.getInt( 1 );
        }

        daoUtil.free(  );

        return nIdUser;
    }

    /**
     * Checks the availibility of an email
     * @param strEmail The email
     * @return user ID if the email is already used by another user, -1 otherwise
     */
    public int checkEmailAlreadyInUse( String strEmail )
    {
        int nIdUser = -1;
        DAOUtil daoUtil = new DAOUtil( SQL_CHECK_EMAIL_IN_USE );
        daoUtil.setString( 1, strEmail );
        daoUtil.executeQuery(  );

        if ( daoUtil.next(  ) )
        {
            nIdUser = daoUtil.getInt( 1 );
        }

        daoUtil.free(  );

        return nIdUser;
    }

    //////////////////////////////////////////////////////////////////
    // for no-module mode
    /**
     * Insert a new record in the table.
     * @param user The AdminUser
     *
     */
    public void insert( LuteceDefaultAdminUser user )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_DEFAULT_USER );

        user.setUserId( newPrimaryKey(  ) );
        daoUtil.setInt( 1, user.getUserId(  ) );
        daoUtil.setString( 2, user.getAccessCode(  ) );
        daoUtil.setString( 3, user.getLastName(  ) );
        daoUtil.setString( 4, user.getFirstName(  ) );
        daoUtil.setString( 5, user.getEmail(  ) );
        daoUtil.setInt( 6, user.getStatus(  ) );
        daoUtil.setString( 7, user.getPassword(  ) );
        daoUtil.setString( 8, user.getLocale(  ).toString(  ) );
        daoUtil.setInt( 9, user.getUserLevel(  ) );
        daoUtil.setBoolean( 10, user.getAccessibilityMode(  ) );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * Store an admin user
     * @param user The AdminUser
     */
    public void store( LuteceDefaultAdminUser user )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_DEFAULT_USER );

        daoUtil.setString( 1, user.getAccessCode(  ) );
        daoUtil.setString( 2, user.getLastName(  ) );
        daoUtil.setString( 3, user.getFirstName(  ) );
        daoUtil.setString( 4, user.getEmail(  ) );
        daoUtil.setInt( 5, user.getStatus(  ) );
        daoUtil.setString( 6, user.getPassword(  ) );
        daoUtil.setString( 7, user.getLocale(  ).toString(  ) );
        daoUtil.setBoolean( 8, user.isPasswordReset(  ) );
        daoUtil.setBoolean( 9, user.getAccessibilityMode(  ) );

        daoUtil.setInt( 10, user.getUserId(  ) );

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * Gets a default admin user
     * @param nUserId the user id
     * @return The user
     */
    public LuteceDefaultAdminUser loadDefaultAdminUser( int nUserId )
    {
        LuteceDefaultAdminUser user = new LuteceDefaultAdminUser(  );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_USER_FROM_USER_ID );
        daoUtil.setInt( 1, nUserId );
        daoUtil.executeQuery(  );

        if ( daoUtil.next(  ) )
        {
            user.setUserId( daoUtil.getInt( 1 ) );
            user.setAccessCode( daoUtil.getString( 2 ) );
            user.setLastName( daoUtil.getString( 3 ) );
            user.setFirstName( daoUtil.getString( 4 ) );
            user.setEmail( daoUtil.getString( 5 ) );
            user.setStatus( daoUtil.getInt( 6 ) );
            user.setPassword( daoUtil.getString( 7 ) );

            Locale locale = new Locale( daoUtil.getString( 8 ) );
            user.setLocale( locale );
            user.setUserLevel( daoUtil.getInt( 9 ) );
            user.setPasswordReset( daoUtil.getBoolean( 10 ) );
            user.setAccessibilityMode( daoUtil.getBoolean( 11 ) );
        }

        daoUtil.free(  );

        return user;
    }

    /**
     * Select all user that own a given role
     * @param strRoleKey The role
     * @return userList The user's list
     */
    public Collection<AdminUser> selectUsersByRole( String strRoleKey )
    {
        Collection<AdminUser> userList = new ArrayList<AdminUser>(  );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_USERS_ID_BY_ROLES );
        daoUtil.setString( 1, strRoleKey );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            AdminUser user = new AdminUser(  );
            user.setUserId( daoUtil.getInt( 1 ) );
            user.setAccessCode( daoUtil.getString( 2 ) );
            user.setLastName( daoUtil.getString( 3 ) );
            user.setFirstName( daoUtil.getString( 4 ) );
            user.setEmail( daoUtil.getString( 5 ) );
            user.setStatus( daoUtil.getInt( 6 ) );
            user.setLocale( new Locale( daoUtil.getString( 7 ) ) );
            user.setAccessibilityMode( daoUtil.getBoolean( 8 ) );
            userList.add( user );
        }

        daoUtil.free(  );

        return userList;
    }

    /**
     * Select all user that own a given level
     * @param nIdLevel The level
     * @return userList The user's list
     */
    public Collection<AdminUser> selectUsersByLevel( int nIdLevel )
    {
        Collection<AdminUser> userList = new ArrayList<AdminUser>(  );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_USERS_BY_LEVEL );
        daoUtil.setInt( 1, nIdLevel );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            AdminUser user = new AdminUser(  );
            user.setUserId( daoUtil.getInt( 1 ) );
            user.setAccessCode( daoUtil.getString( 2 ) );
            user.setLastName( daoUtil.getString( 3 ) );
            user.setFirstName( daoUtil.getString( 4 ) );
            user.setEmail( daoUtil.getString( 5 ) );
            user.setStatus( daoUtil.getInt( 6 ) );
            user.setLocale( new Locale( daoUtil.getString( 7 ) ) );
            user.setAccessibilityMode( daoUtil.getBoolean( 8 ) );
            userList.add( user );
        }

        daoUtil.free(  );

        return userList;
    }

    /**
     * Select rights by user, by user level and by type (Delegated or own)
     *
     * @param nUserId the id of the user
     * @param nUserLevel the id of the user level
     * @param bDelegated true if select concern delegated rights
     * @return collection of id rights
     */
    private Collection<String> selectIdRights( int nUserId, int nUserLevel, boolean bDelegated )
    {
        String strSqlQuery = bDelegated ? SQL_QUERY_SELECT_USER_RIGHTS_DELEGATED : SQL_QUERY_SELECT_USER_RIGHTS_OWN;
        Collection<String> idRightList = new ArrayList<String>(  );
        DAOUtil daoUtil = new DAOUtil( strSqlQuery );
        daoUtil.setInt( 1, nUserId );
        daoUtil.setInt( 2, nUserLevel );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            idRightList.add( daoUtil.getString( 1 ) );
        }

        daoUtil.free(  );

        return idRightList;
    }

    /**
     * Deletes rights by user and by id right
     *
     * @param nUserId the user id
     * @param idRightList the list of rights to delete
     */
    private void deleteRightsForUser( int nUserId, Collection<String> idRightList )
    {
        for ( String strIdRight : idRightList )
        {
            DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_USER_RIGHTS );
            daoUtil.setInt( 1, nUserId );
            daoUtil.setString( 2, strIdRight );
            daoUtil.executeUpdate(  );
            daoUtil.free(  );
        }
    }

    /**
     * Deletes rights own by user ie rights with level >= userlevel
     * @param nUserId the user id
     * @param nUserLevel the user level
     */
    public void deleteAllOwnRightsForUser( int nUserId, int nUserLevel )
    {
        Collection<String> idRightList = selectIdRights( nUserId, nUserLevel, false );

        deleteRightsForUser( nUserId, idRightList );
    }

    /**
     * Deletes rights delegated by user ie rights with level < userlevel
     * @param nUserId the user id
     * @param nUserLevel the user level
     */
    public void deleteAllDelegatedRightsForUser( int nUserId, int nUserLevel )
    {
        Collection<String> idRightList = selectIdRights( nUserId, nUserLevel, true );

        deleteRightsForUser( nUserId, idRightList );
    }

    /**
     * Update role key if role key name has change
     * @param strOldRoleKey The old role key name
     * @param role The new role key
     */
    public void storeUsersRole( String strOldRoleKey, AdminRole role )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_USERS_ROLE );
        daoUtil.setString( 1, role.getKey(  ) );
        daoUtil.setString( 2, strOldRoleKey );

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * Check if the user has the role
     * @param nUserId The ID of the user
     * @param strRoleKey The role Key
     * @return true if the user has the role
     */
    public boolean hasRole( int nUserId, String strRoleKey )
    {
        boolean bHasRole = false;
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_USER_ROLE );
        daoUtil.setInt( 1, nUserId );
        daoUtil.setString( 2, strRoleKey );
        daoUtil.executeQuery(  );

        if ( daoUtil.next(  ) )
        {
            bHasRole = true;
        }

        daoUtil.free(  );

        return bHasRole;
    }

    /**
     * Remove role for an user
     * @param nUserId The ID of the user
     * @param strRoleKey The role key
     */
    public void deleteRoleForUser( int nUserId, String strRoleKey )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_ROLE_FOR_USER );
        daoUtil.setInt( 1, nUserId );
        daoUtil.setString( 2, strRoleKey );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
    * Gets a collection of AdminUser by using a filter.
    * @param auFilter The filter
    * @return The user List
    */
    public Collection<AdminUser> selectUsersByFilter( AdminUserFilter auFilter )
    {
        Collection<AdminUser> userList = new ArrayList<AdminUser>(  );
        DAOUtil daoUtil;

        String query = SQL_QUERY_SELECT_USER_FROM_SEARCH;

        if ( auFilter.getStatus(  ) != -1 )
        {
            query += CONSTANT_AND_STATUS;
        }

        if ( auFilter.getUserLevel(  ) != -1 )
        {
            query += CONSTANT_AND_USER_LEVEL;
        }

        query += CONSTANT_ORDER_BY_LAST_NAME;

        daoUtil = new DAOUtil( query );
        daoUtil.setString( 1, CONSTANT_PERCENT + auFilter.getAccessCode(  ) + CONSTANT_PERCENT );
        daoUtil.setString( 2, CONSTANT_PERCENT + auFilter.getLastName(  ) + CONSTANT_PERCENT );
        daoUtil.setString( 3, CONSTANT_PERCENT + auFilter.getFirstName(  ) + CONSTANT_PERCENT );
        daoUtil.setString( 4, CONSTANT_PERCENT + auFilter.getEmail(  ) + CONSTANT_PERCENT );

        if ( auFilter.getStatus(  ) != -1 )
        {
            daoUtil.setInt( 5, auFilter.getStatus(  ) );

            if ( auFilter.getUserLevel(  ) != -1 )
            {
                daoUtil.setInt( 6, auFilter.getUserLevel(  ) );
            }
        }
        else
        {
            if ( auFilter.getUserLevel(  ) != -1 )
            {
                daoUtil.setInt( 5, auFilter.getUserLevel(  ) );
            }
        }

        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            AdminUser user = new AdminUser(  );
            user.setUserId( daoUtil.getInt( 1 ) );
            user.setAccessCode( daoUtil.getString( 2 ) );
            user.setLastName( daoUtil.getString( 3 ) );
            user.setFirstName( daoUtil.getString( 4 ) );
            user.setEmail( daoUtil.getString( 5 ) );
            user.setStatus( daoUtil.getInt( 6 ) );
            user.setLocale( new Locale( daoUtil.getString( 7 ) ) );
            user.setUserLevel( daoUtil.getInt( 8 ) );
            user.setAccessibilityMode( daoUtil.getBoolean( 9 ) );
            userList.add( user );
        }

        daoUtil.free(  );

        return userList;
    }

    /**
     * Get all users having a given right
     * @param strIdRight The ID right
     * @return A collection of AdminUser
     */
    public Collection<AdminUser> selectUsersByRight( String strIdRight )
    {
        Collection<AdminUser> userList = new ArrayList<AdminUser>(  );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_USERS_BY_RIGHT );
        daoUtil.setString( 1, strIdRight );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            AdminUser user = new AdminUser(  );
            user.setUserId( daoUtil.getInt( 1 ) );
            user.setAccessCode( daoUtil.getString( 2 ) );
            user.setLastName( daoUtil.getString( 3 ) );
            user.setFirstName( daoUtil.getString( 4 ) );
            user.setEmail( daoUtil.getString( 5 ) );
            user.setStatus( daoUtil.getInt( 6 ) );
            user.setLocale( new Locale( daoUtil.getString( 7 ) ) );
            user.setUserLevel( daoUtil.getInt( 8 ) );
            user.setAccessibilityMode( daoUtil.getBoolean( 9 ) );
            userList.add( user );
        }

        daoUtil.free(  );

        return userList;
    }

    /**
    * Check if the user has the given right
    * @param nUserId The ID of the user
    * @param strIdRight The ID right
    * @return true if the user has the right
    */
    public boolean hasRight( int nUserId, String strIdRight )
    {
        boolean bHasRight = false;
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_USER_RIGHT );
        daoUtil.setInt( 1, nUserId );
        daoUtil.setString( 2, strIdRight );
        daoUtil.executeQuery(  );

        if ( daoUtil.next(  ) )
        {
            bHasRight = true;
        }

        daoUtil.free(  );

        return bHasRight;
    }

    /**
     * Remove a right for an user
     * @param nUserId The user ID
     * @param strIdRight The right ID
     */
    public void deleteRightForUser( int nUserId, String strIdRight )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_USER_RIGHTS );
        daoUtil.setInt( 1, nUserId );
        daoUtil.setString( 2, strIdRight );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }
}
