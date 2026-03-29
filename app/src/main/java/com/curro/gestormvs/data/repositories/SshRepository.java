package com.curro.gestormvs.data.repositories;


import android.util.Log;

import com.curro.gestormvs.data.mappers.VirtualMachineMapper;
import com.curro.gestormvs.domain.models.Host;
import com.curro.gestormvs.domain.models.VirtualMachine;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Channel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class SshRepository {

    private Session session;


    // SSH connection
    public void connect(Host host) {
        disconnect();

        JSch jsch = new JSch();

        try {
            session = jsch.getSession(host.getUser(), host.getIp(), host.getPort());
        } catch (Exception e) {
            throw new RuntimeException("Error creating ssh session");
        }

        session.setPassword(host.getPassword());

        // Security config: avoid host key verification
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        try {
            session.connect(10000);
        } catch (JSchException e) {
            throw new RuntimeException("Error connecting to ssh server");
        }

    }

    // SSH disconnection
    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            session = null;
        }
    }


    // Basic virsh commands
    public List<VirtualMachine> getAllVMs() {

        String command = "virsh --connect qemu:///system list --all";
        Channel channel = null;

        try {
            // Open execution channel
            channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            channel.connect();

            // Reading the response
            try (InputStream in = channel.getInputStream()) {

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                StringBuilder rawOutput = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    rawOutput.append(line).append("\n");
                }
                // Reemplaza la línea del Log.d por esto
                String output = rawOutput.toString();
                int chunkSize = 3000;
                for (int i = 0; i < output.length(); i += chunkSize) {
                    Log.d("SshRepository", output.substring(i, Math.min(i + chunkSize, output.length())));
                }


                BufferedReader readerForParser = new BufferedReader(
                        new java.io.StringReader(rawOutput.toString())
                );
                return VirtualMachineMapper.parseOutputToVMsList(readerForParser);
            } catch (IOException e) {
                throw new RuntimeException("Error reading response");
            }

        } catch (JSchException e) {
            throw new RuntimeException("Error creating channel");
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
        }

    }

}
