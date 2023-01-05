/*
 *
 * The DbUnit Database Testing Framework
 * Copyright (C)2002-2004, DbUnit.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.dbunit.database.statement;

import java.sql.SQLException;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.TypeCastException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Manuel Laflamme
 * @since Jun 12, 2003
 * @version $Revision$
 */
public class AutomaticPreparedBatchStatement implements IPreparedBatchStatement
{

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(AutomaticPreparedBatchStatement.class);

    private final IPreparedBatchStatement _statement;
    private final IDatabaseConnection _connection;
    private int _batchCount = 0;
    private int _threshold;
    private int _result = 0;

    public AutomaticPreparedBatchStatement(IDatabaseConnection connection, IPreparedBatchStatement statement, int threshold)
    {
        _connection = connection;
        _statement = statement;
        _threshold = threshold;
    }

    ////////////////////////////////////////////////////////////////////////////
    // IPreparedBatchStatement interface

    public void addValue(Object value, DataType dataType) throws TypeCastException,
            SQLException
    {
        logger.debug("addValue(value={}, dataType={}) - start", value, dataType);

        _statement.addValue(value, dataType);
    }

    public void addBatch() throws SQLException
    {
        logger.debug("addBatch() - start");

        _statement.addBatch();
        _batchCount++;

        if (_batchCount % _threshold == 0)
        {
            _result += _statement.executeBatch();
            if ( _connection.getConnection().getAutoCommit()==false) {
                _connection.getConnection().commit();
                if (logger.isDebugEnabled()) {
                    logger.info("The batch completed - COMMIT ");
                }
            }
            else {
                if (logger.isDebugEnabled())
                {
                    logger.debug("The batch completed - AUTOCOMMIT ");
                }
            }
        }
    }

    public int executeBatch() throws SQLException
    {
        logger.debug("executeBatch() - start");

        _result += _statement.executeBatch();
        if ( _connection.getConnection().getAutoCommit()==false) {
            _connection.getConnection().commit();
            if (logger.isDebugEnabled()) {
                logger.info("The batch completed - COMMIT ");
            }
        }
        else {
            if (logger.isDebugEnabled())
            {
                logger.debug("The batch completed - AUTOCOMMIT ");
            }
        }
        return _result;
    }

    public void clearBatch() throws SQLException
    {
        logger.debug("clearBatch() - start");

        _statement.clearBatch();
        _batchCount = 0;
    }

    public void close() throws SQLException
    {
        logger.debug("close() - start");

        _statement.close();
    }
}
