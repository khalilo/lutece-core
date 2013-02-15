package fr.paris.lutece.portal.service.csv;

import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.business.physicalfile.PhysicalFile;
import fr.paris.lutece.portal.business.physicalfile.PhysicalFileHome;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.fileupload.FileItem;

import au.com.bytecode.opencsv.CSVReader;


/**
 * Service to get data from a CSV file. The CSV might be a physical file, or a
 * memory file.
 * Implementations can either be statefull or stateless.
 */
public abstract class CSVReaderService
{
    private static final String MESSAGE_NO_FILE_FOUND = "portal.util.message.noFileFound";
    private static final String MESSAGE_ERROR_READING_FILE = "portal.util.message.errorReadingFile";
    private static final String MESSAGE_ERROR_NUMBER_COLUMNS = "portal.xsl.message.errorNumberColumns";
    private static final String MESSAGE_UNKOWN_ERROR = "portal.xsl.message.errorUnknown";

    private static final String PROPERTY_DEFAULT_CSV_SEPARATOR = "lutece.csvReader.defaultCSVSeparator";

    private static final String CONSTANT_DEFAULT_CSV_SEPARATOR = ";";

    private Character _strCSVSeparator;

    /**
     * Read a line of the CSV file.
     * @param strLineDataArray The content of the line of the CSV file.
     * @param nLineNumber Number of the current line
     * @param locale the locale
     * @return Returns the list of messages associated with the line.
     */
    protected abstract List<CSVMessageDescriptor> readLineOfCSVFile( String[] strLineDataArray, int nLineNumber,
            Locale locale );

    /**
     * Check the line of the CSV file. This method is called once on each line
     * of the file if the number of columns is correct. If the file is entirely
     * checked before processing, then this method is called before any line is
     * processed. Otherwise it is called just before the processing of the line.
     * @param strLineDataArray The content of the line of the CSV file.
     * @param nLineNumber Number of the current line
     * @param locale the locale
     * @return The list of messages of the lines. <strong>Lines that contain
     *         messages with messages levels other than
     *         {@link CSVMessageLevel#INFO INFO} will NOT be processed, and the
     *         global processing may stop if the ExitOnError flag has been set
     *         to true !</strong>
     */
    protected abstract List<CSVMessageDescriptor> checkLineOfCSVFile( String[] strLineDataArray, int nLineNumber,
            Locale locale );

    /**
     * Get messages after the process is completed.
     * @param nNbLineParses The number of lines parses. If the first line was
     *            skipped, it is not counted.
     * @param nNbLinesWithoutErrors the number of lines parses whitout error.
     * @param locale The locale
     * @return A list of messages.
     */
    protected abstract List<CSVMessageDescriptor> getEndOfProcessMessages( int nNbLineParses,
            int nNbLinesWithoutErrors, Locale locale );

    /**
     * Get the default CSV separator to use. If the property of the default
     * separator to use is not set, then the semi-colon is returned.
     * @return the default CSV separator to use
     */
    public static Character getDefaultCSVSeparator( )
    {
        return AppPropertiesService.getProperty( PROPERTY_DEFAULT_CSV_SEPARATOR, CONSTANT_DEFAULT_CSV_SEPARATOR )
                .charAt( 0 );
    }

    /**
     * Read a CSV file and call the method {@link #readLineOfCSVFile(String[])
     * readLineOfCSVFile} for each of its lines.
     * @param fileItem FileItem to get the CSV file from. If the creation of the
     *            input stream associated to this file throws a IOException,
     *            then an error is returned and the file is not red.
     * @param nColumnNumber Number of columns of each lines. Use 0 to skip
     *            column number check (for example if every lines don't have the
     *            same number of columns)
     * @param bCheckFileBeforeProcessing Indicates if the file should be check
     *            before processing any of its line. If it is set to true, then
     *            then no line is processed if the file has any error.
     * @param bExitOnError Indicates if the processing of the CSV file should
     *            end on the first error, or at the end of the file.
     * @param bSkipFirstLine Indicates if the first line of the file should be
     *            skipped or not.
     * @param locale the locale
     * @return Returns the list of errors that occurred during the processing of
     *         the file. The returned list is sorted
     * @see CSVMessageDescriptor#compareTo(CSVMessageDescriptor)
     *      CSVMessageDescriptor.compareTo(CSVMessageDescriptor) for information
     *      about sort
     */
    public List<CSVMessageDescriptor> readCSVFile( FileItem fileItem, int nColumnNumber,
            boolean bCheckFileBeforeProcessing, boolean bExitOnError, boolean bSkipFirstLine, Locale locale )
    {
        if ( fileItem != null )
        {
            InputStreamReader inputStreamReader = null;
            try
            {
                inputStreamReader = new InputStreamReader( fileItem.getInputStream( ) );
            }
            catch ( IOException e )
            {
                AppLogService.error( e.getMessage( ), e );
            }
            if ( inputStreamReader != null )
            {
                CSVReader csvReader = new CSVReader( inputStreamReader, getCSVSeparator( ) );
                return readCSVFile( csvReader, nColumnNumber, bCheckFileBeforeProcessing, bExitOnError, bSkipFirstLine,
                        locale );
            }
        }
        List<CSVMessageDescriptor> listErrors = new ArrayList<CSVMessageDescriptor>( );
        CSVMessageDescriptor errorDescription = new CSVMessageDescriptor( CSVMessageLevel.ERROR, 0,
                I18nService.getLocalizedString( MESSAGE_NO_FILE_FOUND, locale ) );
        listErrors.add( errorDescription );
        return listErrors;
    }

    /**
     * Read a CSV file and call the method {@link #readLineOfCSVFile(String[])
     * readLineOfCSVFile} for each of its lines.
     * @param strPath Path if the file to read in the file system.
     * @param nColumnNumber Number of columns of each lines. Use 0 to skip
     *            column number check (for example if every lines don't have the
     *            same number of columns)
     * @param bCheckFileBeforeProcessing Indicates if the file should be check
     *            before processing any of its line. If it is set to true, then
     *            then no line is processed if the file has any error.
     * @param bExitOnError Indicates if the processing of the CSV file should
     *            end on the first error, or at the end of the file.
     * @param bSkipFirstLine Indicates if the first line of the file should be
     *            skipped or not.
     * @param locale the locale
     * @return Returns the list of errors that occurred during the processing of
     *         the file. The returned list is sorted
     * @see CSVMessageDescriptor#compareTo(CSVMessageDescriptor)
     *      CSVMessageDescriptor.compareTo(CSVMessageDescriptor) for information
     *      about sort
     */
    public List<CSVMessageDescriptor> readCSVFile( String strPath, int nColumnNumber,
            boolean bCheckFileBeforeProcessing, boolean bExitOnError, boolean bSkipFirstLine, Locale locale )
    {
        java.io.File file = new java.io.File( strPath );
        try
        {
            FileReader fileReader = new FileReader( file );
            CSVReader csvReader = new CSVReader( fileReader, getCSVSeparator( ) );
            return readCSVFile( csvReader, nColumnNumber, bCheckFileBeforeProcessing, bExitOnError, bSkipFirstLine,
                    locale );
        }
        catch ( FileNotFoundException e )
        {
            AppLogService.error( e.getMessage( ), e );
        }
        List<CSVMessageDescriptor> listErrors = new ArrayList<CSVMessageDescriptor>( );
        CSVMessageDescriptor errorDescription = new CSVMessageDescriptor( CSVMessageLevel.ERROR, 0,
                I18nService.getLocalizedString( MESSAGE_NO_FILE_FOUND, locale ) );
        listErrors.add( errorDescription );
        return listErrors;
    }

    /**
     * Read a CSV file and call the method {@link #readLineOfCSVFile(String[])
     * readLineOfCSVFile} for each
     * of its lines.
     * @param file File to get the values from. If the physical file of this
     *            file has no value, then it is gotten from the database.
     * @param nColumnNumber Number of columns of each lines. Use 0 to skip
     *            column number check (for example if every lines don't have the
     *            same number of columns)
     * @param bCheckFileBeforeProcessing Indicates if the file should be check
     *            before processing any of its line. If it is set to true, then
     *            then no line is processed if the file has any error.
     * @param bExitOnError Indicates if the processing of the CSV file should
     *            end on the first error, or at the end of the file.
     * @param bSkipFirstLine Indicates if the first line of the file should be
     *            skipped or not.
     * @param locale the locale
     * @return Returns the list of errors that occurred during the processing of
     *         the file. The returned list is sorted
     * @see CSVMessageDescriptor#compareTo(CSVMessageDescriptor)
     *      CSVMessageDescriptor.compareTo(CSVMessageDescriptor) for information
     *      about sort
     */
    public List<CSVMessageDescriptor> readCSVFile( File file, int nColumnNumber, boolean bCheckFileBeforeProcessing,
            boolean bExitOnError, boolean bSkipFirstLine, Locale locale )
    {

        return readCSVFile( file.getPhysicalFile( ), nColumnNumber, bCheckFileBeforeProcessing, bExitOnError,
                bSkipFirstLine, locale );

    }

    /**
     * Read a CSV file and call the method {@link #readLineOfCSVFile(String[])
     * readLineOfCSVFile} for each
     * of its lines.
     * @param physicalFile The physicalFile to get the values from. If the
     *            physical file has no value, then it is gotten from the
     *            database.
     * @param nColumnNumber Number of columns of each lines. Use 0 to skip
     *            column number check (for example if every lines don't have the
     *            same number of columns)
     * @param bCheckFileBeforeProcessing Indicates if the file should be check
     *            before processing any of its line. If it is set to true, then
     *            then no line is processed if the file has any error.
     * @param bExitOnError Indicates if the processing of the CSV file should
     *            end on the first error, or at the end of the file.
     * @param bSkipFirstLine Indicates if the first line of the file should be
     *            skipped or not.
     * @param locale the locale
     * @return Returns the list of errors that occurred during the processing of
     *         the file. The returned list is sorted
     * @see CSVMessageDescriptor#compareTo(CSVMessageDescriptor)
     *      CSVMessageDescriptor.compareTo(CSVMessageDescriptor) for information
     *      about sort
     */
    public List<CSVMessageDescriptor> readCSVFile( PhysicalFile physicalFile, int nColumnNumber,
            boolean bCheckFileBeforeProcessing, boolean bExitOnError, boolean bSkipFirstLine, Locale locale )
    {
        if ( physicalFile != null && physicalFile.getValue( ) == null )
        {
            if ( physicalFile.getValue( ) == null )
            {
                physicalFile = PhysicalFileHome.findByPrimaryKey( physicalFile.getIdPhysicalFile( ) );
            }
            if ( physicalFile != null && physicalFile.getValue( ) == null )
            {
                InputStream inputStream = new ByteArrayInputStream( physicalFile.getValue( ) );
                InputStreamReader inputStreamReader = new InputStreamReader( inputStream );
                CSVReader csvReader = new CSVReader( inputStreamReader, getCSVSeparator( ) );
                return readCSVFile( csvReader, nColumnNumber, bCheckFileBeforeProcessing, bExitOnError, bSkipFirstLine,
                        locale );
            }
        }
        List<CSVMessageDescriptor> listErrors = new ArrayList<CSVMessageDescriptor>( );
        CSVMessageDescriptor errorDescription = new CSVMessageDescriptor( CSVMessageLevel.ERROR, 0,
                I18nService.getLocalizedString( MESSAGE_NO_FILE_FOUND, locale ) );
        listErrors.add( errorDescription );
        return listErrors;
    }

    /**
     * Read a CSV file and call the method {@link #readLineOfCSVFile(String[])
     * readLineOfCSVFile} for each
     * of its lines.
     * @param csvReader CSV reader to use to read the CSV file
     * @param nColumnNumber Number of columns of each lines. Use 0 to skip
     *            column number check (for example if every lines don't have the
     *            same number of columns)
     * @param bCheckFileBeforeProcessing Indicates if the file should be check
     *            before processing any of its line. If it is set to true, then
     *            then no line is processed if the file has any error.
     * @param bExitOnError Indicates if the processing of the CSV file should
     *            end on the first error, or at the end of the file.
     * @param bSkipFirstLine Indicates if the first line of the file should be
     *            skipped or not.
     * @param locale the locale
     * @return Returns the list of errors that occurred during the processing of
     *         the file. The returned list is sorted
     * @see CSVMessageDescriptor#compareTo(CSVMessageDescriptor)
     *      CSVMessageDescriptor.compareTo(CSVMessageDescriptor) for information
     *      about sort
     */
    protected List<CSVMessageDescriptor> readCSVFile( CSVReader csvReader, int nColumnNumber,
            boolean bCheckFileBeforeProcessing, boolean bExitOnError, boolean bSkipFirstLine, Locale locale )
    {
        List<CSVMessageDescriptor> listMessages = new ArrayList<CSVMessageDescriptor>( );
        int nLineNumber = 0;
        if ( bSkipFirstLine )
        {
            try
            {
                nLineNumber++;
                csvReader.readNext( );
            }
            catch ( IOException e )
            {
                AppLogService.error( e.getMessage( ), e );
                CSVMessageDescriptor error = new CSVMessageDescriptor( CSVMessageLevel.ERROR, 1,
                        I18nService.getLocalizedString( MESSAGE_ERROR_READING_FILE, locale ) );
                listMessages.add( error );
                if ( bExitOnError )
                {
                    try
                    {
                        csvReader.close( );
                    }
                    catch ( IOException ex )
                    {
                        AppLogService.error( ex.getMessage( ), ex );
                    }
                    return listMessages;
                }
            }
        }

        List<String[]> listLines = null;
        if ( bCheckFileBeforeProcessing )
        {
            listLines = new ArrayList<String[]>( );
            String[] strLine = null;
            do
            {
                try
                {
                    nLineNumber++;
                    strLine = csvReader.readNext( );
                }
                catch ( IOException e )
                {
                    AppLogService.error( e.getMessage( ), e );
                    CSVMessageDescriptor error = new CSVMessageDescriptor( CSVMessageLevel.ERROR, nLineNumber,
                            I18nService.getLocalizedString( MESSAGE_ERROR_READING_FILE, locale ) );
                    listMessages.add( error );
                    if ( bExitOnError )
                    {
                        try
                        {
                            csvReader.close( );
                        }
                        catch ( IOException ex )
                        {
                            AppLogService.error( ex.getMessage( ), ex );
                        }
                        Collections.sort( listMessages );
                        return listMessages;
                    }
                }
                listLines.add( strLine );
            }
            while ( strLine != null );
            List<CSVMessageDescriptor> listCheckErrors = checkCSVFileValidity( listLines, nColumnNumber,
                    bSkipFirstLine, locale );
            if ( listCheckErrors.size( ) > 0 )
            {
                if ( doesListMessageContainError( listCheckErrors ) )
                {
                    listCheckErrors.addAll( 0, listMessages );
                    try
                    {
                        csvReader.close( );
                    }
                    catch ( IOException ex )
                    {
                        AppLogService.error( ex.getMessage( ), ex );
                    }
                    Collections.sort( listMessages );
                    return listCheckErrors;
                }
            }
            nLineNumber = 0;
        }

        boolean bHasMoreLines = true;
        int nNbLinesWithoutErrors = 0;
        String[] strLine = null;
        Iterator<String[]> iterator = null;
        if ( listLines != null )
        {
            iterator = listLines.iterator( );
        }
        while ( bHasMoreLines )
        {

            nLineNumber++;
            if ( iterator != null )
            {
                if ( iterator.hasNext( ) )
                {
                    strLine = iterator.next( );
                }
                else
                {
                    strLine = null;
                    bHasMoreLines = false;
                }
            }
            else
            {
                try
                {
                    strLine = csvReader.readNext( );
                }
                catch ( IOException e )
                {
                    strLine = null;
                    AppLogService.error( e.getMessage( ), e );
                    CSVMessageDescriptor error = new CSVMessageDescriptor( CSVMessageLevel.ERROR, nLineNumber,
                            I18nService.getLocalizedString( MESSAGE_ERROR_READING_FILE, locale ) );
                    listMessages.add( error );
                    if ( bExitOnError )
                    {
                        bHasMoreLines = false;
                    }
                }
            }
            if ( strLine != null )
            {
                try
                {
                    List<CSVMessageDescriptor> listLinesMessages = null;
                    if ( !bCheckFileBeforeProcessing )
                    {
                        listLinesMessages = checkCSVLineColumnNumber( strLine, nColumnNumber, nLineNumber, locale );
                        if ( !doesListMessageContainError( listLinesMessages ) )
                        {
                            List<CSVMessageDescriptor> listFileCheckMessages = checkLineOfCSVFile( strLine,
                                    nLineNumber, locale );
                            if ( listFileCheckMessages != null && listFileCheckMessages.size( ) > 0 )
                            {
                                if ( listLinesMessages != null && listLinesMessages.size( ) > 0 )
                                {
                                    listLinesMessages.addAll( listFileCheckMessages );
                                }
                                else
                                {
                                    listLinesMessages = listFileCheckMessages;
                                }
                            }
                        }
                        if ( listLinesMessages != null && listLinesMessages.size( ) > 0 )
                        {
                            listMessages.addAll( listLinesMessages );
                        }
                    }
                    // If the line has no error
                    if ( !doesListMessageContainError( listLinesMessages ) )
                    {
                        List<CSVMessageDescriptor> listMessagesOfCurrentLine = readLineOfCSVFile( strLine, nLineNumber,
                                locale );
                        if ( listMessagesOfCurrentLine != null && listMessagesOfCurrentLine.size( ) > 0 )
                        {
                            listMessages.addAll( listMessagesOfCurrentLine );
                        }
                        if ( doesListMessageContainError( listMessagesOfCurrentLine ) )
                        {
                            if ( bExitOnError )
                            {
                                bHasMoreLines = false;
                            }
                        }
                        else
                        {
                            nNbLinesWithoutErrors++;
                        }
                    }
                }
                catch ( Exception e )
                {
                    AppLogService.error( e.getMessage( ), e );
                    CSVMessageDescriptor error = new CSVMessageDescriptor( CSVMessageLevel.ERROR, nLineNumber,
                            I18nService.getLocalizedString( MESSAGE_UNKOWN_ERROR, locale ) );
                    listMessages.add( error );
                    if ( bExitOnError )
                    {
                        bHasMoreLines = false;
                    }
                }
            }
            else
            {
                bHasMoreLines = false;
            }
        }
        try
        {
            csvReader.close( );
        }
        catch ( IOException ex )
        {
            AppLogService.error( ex.getMessage( ), ex );
        }
        // We incremented the line number for the last line that didn't exist
        nLineNumber--;
        if ( bSkipFirstLine )
        {
            nLineNumber--;
        }
        List<CSVMessageDescriptor> listMessagesEndOfProcess = getEndOfProcessMessages( nLineNumber,
                nNbLinesWithoutErrors, locale );
        if ( listMessagesEndOfProcess != null && listMessagesEndOfProcess.size( ) > 0 )
        {
            listMessages.addAll( 0, listMessagesEndOfProcess );
        }
        Collections.sort( listMessages );
        return listMessages;
    }

    /**
     * Check the validity of the whole CSV file.
     * @param listLines The list of lines of the file
     * @param nColumnNumber The number of columns every line must have.
     * @param bSkipFirstLine True if the first line should be ignored, false
     *            otherwise
     * @param locale The locale
     * @return Returns a list of errors found in the file.
     */
    protected List<CSVMessageDescriptor> checkCSVFileValidity( List<String[]> listLines, int nColumnNumber,
            boolean bSkipFirstLine, Locale locale )
    {
        List<CSVMessageDescriptor> listErrors = new ArrayList<CSVMessageDescriptor>( );
        int nLineNumber = 0;
        if ( bSkipFirstLine )
        {
            nLineNumber++;
        }
        for ( String[] strLine : listLines )
        {
            nLineNumber++;
            List<CSVMessageDescriptor> listMessages = checkCSVLineColumnNumber( strLine, nColumnNumber, nLineNumber,
                    locale );
            if ( listMessages != null && listMessages.size( ) > 0 )
            {
                listErrors.addAll( listMessages );
            }
            if ( !doesListMessageContainError( listMessages ) )
            {
                checkLineOfCSVFile( strLine, nLineNumber, locale );
            }
        }
        return listErrors;
    }

    /**
     * Check the number of columns of a line.
     * @param strLine The line to check
     * @param nColumnNumber The number of columns the line must have
     * @param nLineNumber The number of the current line
     * @param locale The locale
     * @return The error if an error is found, or null if there is none.
     */
    protected List<CSVMessageDescriptor> checkCSVLineColumnNumber( String[] strLine, int nColumnNumber,
            int nLineNumber, Locale locale )
    {
        if ( strLine == null || ( nColumnNumber > 0 && strLine.length != nColumnNumber ) )
        {
            List<CSVMessageDescriptor> listMessages = new ArrayList<CSVMessageDescriptor>( );
            Object[] args = { strLine.length, nColumnNumber };
            String strErrorMessage = I18nService.getLocalizedString( MESSAGE_ERROR_NUMBER_COLUMNS, args, locale );
            CSVMessageDescriptor error = new CSVMessageDescriptor( CSVMessageLevel.ERROR, nLineNumber, strErrorMessage );
            listMessages.add( error );
            return listMessages;
        }
        return null;
    }

    /**
     * Get the separator used for CSV files. If no separator has been set, then
     * the default CSV separator is used.
     * @return the separator used for CSV files, of the default one if non has
     *         been set.
     */
    public Character getCSVSeparator( )
    {
        if ( this._strCSVSeparator == null )
        {
            this._strCSVSeparator = getDefaultCSVSeparator( );
        }
        return _strCSVSeparator;
    }

    /**
     * Set the separator to use for CSV files.
     * @param strSeparator The separator to use for CSV files.
     */
    public void setCSVSeparator( Character strCSVSeparator )
    {
        this._strCSVSeparator = strCSVSeparator;
    }

    /**
     * Check if a list of messages contains messages with the
     * {@link CSVMessageLevel#ERROR ERROR} level
     * @param listMessageOfCurrentLine The list of messages. The list might be
     *            null or empty.
     * @return True if an error is found, false otherwise
     */
    private boolean doesListMessageContainError( List<CSVMessageDescriptor> listMessageOfCurrentLine )
    {
        if ( listMessageOfCurrentLine != null && listMessageOfCurrentLine.size( ) > 0 )
        {
            for ( CSVMessageDescriptor message : listMessageOfCurrentLine )
            {
                if ( message.getMessageLevel( ) == CSVMessageLevel.ERROR )
                {
                    return true;
                }
            }
        }
        return false;
    }
}
