package de.buw.tmdt.plasma.converter.csv;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.buw.tmdt.plasma.utilities.misc.fuse.SimpleFuse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TabularProcessor implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(TabularProcessor.class);

    private static final long serialVersionUID = 3616375212009532992L;
    //Constructor parameters
    private final int headerHeight;
    //Runtime parameters
    private final ArrayList<String[]> headerRows = new ArrayList<>();
    private final SimpleFuse headerCreated = new SimpleFuse();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private int columnAmount = 0;
    private int headerCounter = 0;
    private ArrayList<String> possibleKeys = new ArrayList<>();

    public TabularProcessor(int headerHeight) {
        this.headerHeight = headerHeight;
    }

    public ObjectNode processRow(@NotNull String[] line) {
        //We check if we already have a header
        if (headerCreated.isSane()) {
            //If header height is 0 and we do not have any header yet, we generate a empty header
            if (headerHeight == 0) {
                String[] emptyHeader = generateHeaderForEmptyHeaderHeight(line);
                headerRows.add(emptyHeader);
                finishHeaderCalculation();
            } else {
                if (headerCounter < headerHeight) {
                    headerRows.add(line);
                    headerCounter++;
                    return null;
                } else {
                    finishHeaderCalculation();
                }
            }
        }

        //Validate the data row
        if (columnAmount == line.length) {

            //Annotate a valid row with data
            ObjectNode rowJsonObj = objectMapper.createObjectNode();
            for (int i = 0; i < line.length; i++) {
                rowJsonObj.put(possibleKeys.get(i), line[i]);
            }
            return rowJsonObj;
        } else if (line.length - columnAmount > 0) {
            log.warn("A row of the incoming data had more columns than expected.");
        } else {
            log.warn("A row of the incoming data had less columns than expected.");
        }
        return null;
    }

    private void finishHeaderCalculation() {
        //Identify the column amount (based on the header)
        columnAmount = identifyColumnAmount(headerRows);

        //Generate the keys for the header
        possibleKeys = identifyKeys(headerRows, columnAmount);

        //Finish the header creation
        headerCreated.destroy();
    }

    private String[] generateHeaderForEmptyHeaderHeight(@NotNull String[] row) {
        String[] emptyHeader = new String[row.length];
        for (int i = 0; i < row.length; i++) {
            emptyHeader[i] = "value" + (i + 1);
        }
        return emptyHeader;
    }

    private int identifyColumnAmount(@NotNull List<String[]> headerRows) {
        int columnAmount = 0;
        for (String[] row : headerRows) {
            columnAmount = Math.max(columnAmount, row.length);
        }
        return columnAmount;
    }

    private ArrayList<String> identifyKeys(@NotNull List<String[]> headerRows, int columnAmount) {
        //Remove invalid header rows
        List<String[]> validHeaderRows = headerRows.stream().filter(x -> x.length == columnAmount).collect(Collectors.toList());

        //Calculate the keys
        ArrayList<String> possibleKeys = new ArrayList<>(); //must be a standard list implementation for serializable
        if (validHeaderRows.size() > 0) {
            for (int i = 0; i < columnAmount; i++) {
                String entityTypeName = "";
                for (String[] headerRow : validHeaderRows) {
                    entityTypeName = entityTypeName.concat(headerRow[i]);
                }
                possibleKeys.add(entityTypeName);
            }
        } else {
            //if no header is given the keys will just be named by their column number
            for (int i = 0; i < columnAmount; i++) {
                possibleKeys.add(Integer.toString(i));
            }
        }
        return possibleKeys;
    }
}