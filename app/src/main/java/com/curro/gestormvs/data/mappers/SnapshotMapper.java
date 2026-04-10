package com.curro.gestormvs.data.mappers;

import com.curro.gestormvs.domain.models.Snapshot;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SnapshotMapper {

    public static List<Snapshot> parseOutputToSnapshotsList (BufferedReader br) throws IOException {
        List<Snapshot> snapshotList = new ArrayList<>();
        String line;
        boolean dataStarted = false;

        while ((line = br.readLine()) != null) {
            line = line.trim();

            // If line is blank, skip it
            if (line.isEmpty()) {
                continue;
            }

            // Data starts after the separator line
            if (line.startsWith("---")) {
                dataStarted = true;
                continue;
            }

            if (!dataStarted) {
                continue;
            }

            String[] columns = line.split("\\s+");
            if (columns.length >= 3) {
                String name = columns[0];
                String creationTime = columns[1] + " " + columns[2];
                snapshotList.add(new Snapshot(name, creationTime));
            }
        }
        return snapshotList;
    }

}
