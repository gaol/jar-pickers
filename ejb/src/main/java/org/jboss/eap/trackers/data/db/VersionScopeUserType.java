/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.eap.trackers.data.db;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StringType;
import org.hibernate.usertype.UserType;
import org.jboss.eap.trackers.data.versioning.VersionRanges;

/**
 * 
 * @author lgao
 *
 */
public class VersionScopeUserType implements UserType {

    @Override
    public Object assemble(Serializable arg0, Object arg1) throws HibernateException {
        return arg0;
    }

    @Override
    public Object deepCopy(Object obj) throws HibernateException {
        if (obj == null)
            return null;
        if (! (obj instanceof VersionRanges)) {
            throw new UnsupportedOperationException("Can't convert: " + obj.getClass().getName());
        }
        return ((VersionRanges)obj).clone();
    }

    @Override
    public Serializable disassemble(Object obj) throws HibernateException {
        return (Serializable)deepCopy(obj);
    }

    @Override
    public boolean equals(Object arg0, Object arg1) throws HibernateException {
       if (arg0 == null) {
          return arg1 == null;
       }
        return arg0.equals(arg1);
    }

    @Override
    public int hashCode(Object arg0) throws HibernateException {
        return arg0.hashCode();
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor arg2, Object arg3) throws HibernateException,
            SQLException {
        String str = rs.getString(names[0]);
        if (str != null && str.length() > 0) {
            return new VersionRanges(str);
        }
        return null;
    }

    @Override
    public void nullSafeSet(PreparedStatement ps, Object value, int index, SessionImplementor session) throws HibernateException,
            SQLException {
        if (value != null) {
            VersionRanges scope = (VersionRanges)value;
            StringType.INSTANCE.nullSafeSet(ps, scope.toString(), index, session);
        }
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class returnedClass() {
        return VersionRanges.class;
    }

    @Override
    public int[] sqlTypes() {
        return new int[]{StringType.INSTANCE.sqlType()};
    }

}
