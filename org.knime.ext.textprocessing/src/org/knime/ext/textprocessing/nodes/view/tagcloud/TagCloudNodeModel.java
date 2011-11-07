/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 2, as 
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ---------------------------------------------------------------------
 *
 * History
 *   15.11.2008 (Iris Adae): created
 */

package org.knime.ext.textprocessing.nodes.view.tagcloud;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.knime.base.data.xml.SvgCell;
import org.knime.base.data.xml.SvgImageContent;
import org.knime.base.node.util.DefaultDataArray;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.image.ImageContent;
import org.knime.core.data.image.png.PNGImageContent;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NodeViewExport;
import org.knime.core.node.NodeViewExport.ExportType;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.port.image.ImagePortObjectSpec;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 * The NodeModel of the tag cloud node.
 *
 * @author Iris Adae, University of Konstanz
 */

public class TagCloudNodeModel extends NodeModel {

    /** stores the input data table. */
    private DataTable m_data;

    /** stores a copy of the  data, needed to show the tagcloud. */
    private TagCloud m_tagcloud;

   private SettingsModelString m_termColModel =
            TagCloudNodeDialog.getTermColumnModel();

    private SettingsModelString m_valueColModel =
            TagCloudNodeDialog.getValueModel();

    private SettingsModelString m_calcTCTypeModel =
            TagCloudNodeDialog.getTypeofTCcalculationModel();

    private SettingsModelBoolean m_ignoretags =
            TagCloudNodeDialog.getBooleanModel();

    private SettingsModelIntegerBounded m_noOfRows =
            TagCloudNodeDialog.getNoofRowsModel();
    
    private SettingsModelBoolean m_allRows =
            TagCloudNodeDialog.getUseallrowsBooleanModel();
    
    private SettingsModelIntegerBounded m_widthModel = 
        TagCloudNodeDialog.getWidthModel();
    
    private SettingsModelIntegerBounded m_heightModel = 
        TagCloudNodeDialog.getHeightModel();
    
    private SettingsModelString m_imagetypeModel = 
        TagCloudNodeDialog.getImageTypeModel();

    /** The selected ID of the Column containing the value. */
    private int m_valueColIndex;

    /** The selected ID of the Column containing the term . */
    private int m_termColIndex;

    /**
     * The name of the configuration file.
     */
    private static final String DATA_FILE_NAME = "tagcloudpoints.data";

    /**
     * The configuration key for the internal model of the Tagcloud.
     */
    public static final String INTERNAL_MODEL = "TagCloudNodel.data";

    public static final int DEFAULT_WIDTH = 700;
    
    public static final int DEFAULT_HEIGHT = 700;
    
    
    
    /**
     * Initializes NodeModel.
     */
     TagCloudNodeModel() {
         super(new PortType[] {BufferedDataTable.TYPE}, 
                 new PortType[] {ImagePortObject.TYPE});
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        DataTableSpec inSpec = (DataTableSpec)inSpecs[0];
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(inSpec);
        /* 
         * Verifies if there are at least one termcell and
         * one numbercell and initializes the selected column indexes
         */
        verifier.verifyMinimumTermCells(1, true);
        verifier.verifyMinimumNumberCells(1, true);
        setColumnindexes(inSpec);
        
        final String imgType = m_imagetypeModel.getStringValue();
        ImagePortObjectSpec outSpec;
        if (imgType.toUpperCase().startsWith("SVG")) {
            outSpec = new ImagePortObjectSpec(SvgCell.TYPE);
        } else {
            outSpec = new ImagePortObjectSpec(PNGImageContent.TYPE);
        }
        return new PortObjectSpec[] {outSpec};
    }

    /** Initializes the column index for the selected term and value
     * column.
     * @param spec the DataTableSpec of the input table
     */
    protected void setColumnindexes(final DataTableSpec spec) {
    DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        m_termColIndex = verifier.getTermCellIndex();
        m_valueColIndex = verifier.getNumberCellIndex();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected final PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {

        BufferedDataTable dataTable = (BufferedDataTable)inData[0];
        int numofRows = dataTable.getRowCount();
        if (!m_allRows.getBooleanValue()) {
            numofRows = Math.min(m_noOfRows.getIntValue(), numofRows);

        }
        if (numofRows <= 0) {
            m_tagcloud = null;
            setWarningMessage("Empty data table, nothing to display");
            return null;
        }
        m_data = new DefaultDataArray(dataTable, 1, numofRows, exec);
        setColumnindexes(dataTable.getDataTableSpec());
        m_termColIndex = dataTable.getDataTableSpec().findColumnIndex(
                        m_termColModel.getStringValue());

        if (m_termColIndex < 0) {
            m_termColIndex = (new DataTableSpecVerifier(dataTable.getSpec()))
            .getTermCellIndex();
        }
        m_valueColIndex = dataTable.getDataTableSpec().findColumnIndex(
                        m_valueColModel.getStringValue());

        if (m_valueColIndex < 0) {
            m_valueColIndex = (new DataTableSpecVerifier(dataTable.getSpec()))
            .getNumberCellIndex();
        }

        m_tagcloud = new TagCloud();
        m_tagcloud.createTagCloud(exec, this);
        exec.setProgress(1, "TagCloud completed");

        
        TagCloudViewPlotter plotter = new TagCloudViewPlotter();
        plotter.setTagCloudModel(m_tagcloud);
        
        final String imgType = m_imagetypeModel.getStringValue();
        final ExportType exportType =
                NodeViewExport.getViewExportMap().get(imgType);
        if (exportType == null) {
            throw new InvalidSettingsException("Invalid image type:" + imgType);
        }
        final File file =
                File.createTempFile("image", "." + exportType.getFileSuffix());
        file.deleteOnExit();
        
        // set the image size before exporting
        
        exec.setMessage("Creating image file...");
        exportType.export(file, plotter, m_widthModel.getIntValue(),
                m_heightModel.getIntValue());
        final InputStream is = new FileInputStream(file);
        ImagePortObjectSpec outSpec;
        final ImageContent image;
        if (imgType.toUpperCase().startsWith("SVG")) {
            outSpec = new ImagePortObjectSpec(SvgCell.TYPE);
            image = new SvgImageContent(is);
        } else {
            outSpec = new ImagePortObjectSpec(PNGImageContent.TYPE);
            image = new PNGImageContent(is);
        }
        is.close();
        file.delete();
        final PortObject po = new ImagePortObject(image, outSpec);
        return new PortObject[]{po};
    }



    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_valueColModel.loadSettingsFrom(settings);
        m_calcTCTypeModel.loadSettingsFrom(settings);
        m_ignoretags.loadSettingsFrom(settings);
        m_termColModel.loadSettingsFrom(settings);
        m_allRows.loadSettingsFrom(settings);
        m_noOfRows.loadSettingsFrom(settings);
        m_widthModel.loadSettingsFrom(settings);
        m_heightModel.loadSettingsFrom(settings);
        m_imagetypeModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // nothing to reset
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {

        // Save tagcloud content
        ModelContent modelContent = new ModelContent(INTERNAL_MODEL);
        m_tagcloud.saveTo(modelContent);

        File file = new File(nodeInternDir, DATA_FILE_NAME);
        FileOutputStream fos = new FileOutputStream(file);
        modelContent.saveToXML(fos);
        fos.close();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {

        File file = new File(nodeInternDir, DATA_FILE_NAME);
        FileInputStream fis = new FileInputStream(file);
        ModelContentRO modelContent = ModelContent.loadFromXML(fis);

        try {
          m_tagcloud = new TagCloud();
          m_tagcloud.loadFrom(modelContent);

        } catch (InvalidSettingsException e1) {
            IOException ioe = new IOException("Could not load settings,"
                    + "due to invalid settings in model content !");
            ioe.initCause(e1);
            fis.close();
            throw ioe;
        }
        fis.close();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_valueColModel.saveSettingsTo(settings);
        m_calcTCTypeModel.saveSettingsTo(settings);
        m_ignoretags.saveSettingsTo(settings);
        m_termColModel.saveSettingsTo(settings);
        m_noOfRows.saveSettingsTo(settings);
        m_allRows.saveSettingsTo(settings);
        m_widthModel.saveSettingsTo(settings);
        m_heightModel.saveSettingsTo(settings);
        m_imagetypeModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_valueColModel.validateSettings(settings);
        m_calcTCTypeModel.validateSettings(settings);
        m_ignoretags.validateSettings(settings);
        m_termColModel.validateSettings(settings);
        m_noOfRows.validateSettings(settings);
        m_allRows.validateSettings(settings);
        m_widthModel.validateSettings(settings);
        m_heightModel.validateSettings(settings);
        m_imagetypeModel.validateSettings(settings);
    }


    /**
     * @return the kind of calculation for the TagCloud
     */
    public String getTCcalcType() {
        return m_calcTCTypeModel.getStringValue();
    }

    /**
     * @return the selected column ID of the value column.
     */
    public int getValueCol() {
        return m_valueColIndex;
    }

    /**
     * @return the input data table
     */
    public DataTable getData() {
        if (m_data != null) {
            return m_data;
        }
        return null;
    }

    /**
     * @return the chosen Column id Containing the Term
     */
    public int getTermCol() {
        return m_termColIndex;
    }


    /**
     * @return the pre calculated TagCloud data
     */
    public TagCloud getTagCloud() {
        return m_tagcloud;
    }

    /**
     * @return true if tags should be ignored
     */
    public boolean ignoreTags() {
        return m_ignoretags.getBooleanValue();
    }

    /**
     * @return the preferred dimensions of the window which is the layout
     * dimension {@link #getLayoutDimension()} plus an offset
     */
    private Dimension getWindowDimension() {
        return new Dimension(m_widthModel.getIntValue(),
                m_heightModel.getIntValue());
    }    
}
