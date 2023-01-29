package de.buw.tmdt.plasma.converter.esri;

import de.buw.tmdt.plasma.converter.ConversionException;
import de.buw.tmdt.plasma.converter.geojson.GeoJSONConverter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ESRIConverter {

    private static final Logger log = LoggerFactory.getLogger(ESRIConverter.class);

    public static final int DECIMALS = 15;
    public static final int BUFFER_SIZE = 1024;

    private ESRIConverter() {

    }

    public static String convertToGeoJSON(Path filePath) throws ConversionException {
        return convertToGeoJSON(filePath, StandardCharsets.ISO_8859_1);
    }

    public static String convertToJSON(Path filePath) throws ConversionException {
        return convertToJSON(filePath, StandardCharsets.ISO_8859_1);
    }

    public static String convertToJSON(Path filePath, Charset charset) throws ConversionException {
        String geoJson = convertToGeoJSON(filePath, charset);
        return GeoJSONConverter.convert(geoJson);
    }

    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    public static String convertToGeoJSON(Path filePath, Charset charset) throws ConversionException {
        String tmpDirectory = "./" + UUID.randomUUID();
        File destDir = null;
        ShapefileDataStore dataStore = null;
        try (InputStream inputStream = new FileInputStream(filePath.toFile())) {

            // Unzip File

            destDir = new File(tmpDirectory);
            if (!destDir.mkdirs()) {
                log.warn("DestDir " + destDir.getAbsolutePath() + " could not be created!");
            }
            byte[] buffer = new byte[BUFFER_SIZE];
            ZipInputStream zis = new ZipInputStream(inputStream);
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destDir, zipEntry);
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();

            String[] shapeFiles = destDir.list((dir, name) -> name.endsWith(".shp"));

            // Read Shape File
            if (shapeFiles == null || shapeFiles.length == 0) {
                throw new ConversionException("No valid *.shp file in ZIP archive.");
            }
            File file = new File(destDir, FilenameUtils.getName(shapeFiles[0]));
            Map<String, Object> map = new HashMap<>();
            map.put("url", file.toURI().toURL());

            dataStore = (ShapefileDataStore) DataStoreFinder.getDataStore(map);

            dataStore.setCharset(charset);
            String typeName = dataStore.getTypeNames()[0];

            FeatureSource<SimpleFeatureType, SimpleFeature> source =
                    dataStore.getFeatureSource(typeName);
            Filter filter = Filter.INCLUDE;

            FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);

            // Parse to GeoJSON
            StringWriter sw = new StringWriter();
            GeometryJSON geometryJSON = new GeometryJSON(DECIMALS);
            FeatureJSON fj = new FeatureJSON(geometryJSON);

            fj.writeFeatureCollection(collection, sw);
            dataStore.dispose();
            return sw.toString();
        } catch (IOException e) {
            throw new ConversionException("Failed to read shape file properly.", e);

        } finally {
            if (dataStore != null) {
                dataStore.dispose();
            }

            if (destDir != null) {
                try {
                    FileUtils.deleteDirectory(destDir);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, FilenameUtils.getName(zipEntry.getName()));
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }
}
