package com.curro.gestormvs.data.mappers;

import com.curro.gestormvs.domain.models.VirtualMachine;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VirtualMachineMapper {

    public static List<VirtualMachine> parseOutputToVMsList (BufferedReader br) throws IOException {
        List<VirtualMachine> vmList = new ArrayList<>();
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
                String idString = columns[0];
                String name = columns[1];

                // State could be multiple words
                StringBuilder stateBuilder = new StringBuilder(columns[2]);
                for (int i = 3; i < columns.length; i++) {
                    stateBuilder.append(" ").append(columns[i]);
                }
                String state = stateBuilder.toString();

                // If id is "-", it will be null
                Integer id = null;
                if (!idString.equals("-")) {
                    try {
                        id = Integer.parseInt(idString);
                    } catch (NumberFormatException e) {
                        // ;
                    }
                }

                VirtualMachine vm = new VirtualMachine(id, name, state);
                vmList.add(vm);
            }
        }

        return vmList;
    }

}
