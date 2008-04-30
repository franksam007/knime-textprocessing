/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * ---------------------------------------------------------------------
 * 
 * History
 *   24.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.tagfilter;

/**
 * Provides the configuration keys of the tag filter node.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class TagFilterConfigKeys {

    /**
     * The configuration key for the strict filtering setting.
     */
    public static final String CFGKEY_STRICT = "Strict";
    
    /**
     * The configuration key for the set of valid tags.
     */
    public static final String CFGKEY_VALIDTAGS = "ValidTags";
}