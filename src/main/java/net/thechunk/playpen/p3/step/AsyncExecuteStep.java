package net.thechunk.playpen.p3.step;

import lombok.extern.log4j.Log4j2;
import net.thechunk.playpen.coordinator.local.Server;
import net.thechunk.playpen.p3.IPackageStep;
import net.thechunk.playpen.p3.PackageContext;
import net.thechunk.playpen.utils.JSONUtils;
import net.thechunk.playpen.utils.process.XProcess;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

@Log4j2
public class AsyncExecuteStep implements IPackageStep {
    @Override
    public String getStepId() {
        return "async-execute";
    }

    @Override
    public boolean runStep(PackageContext ctx, JSONObject config) {
        Server server = null;
        if(ctx.getUser() instanceof Server) {
            server = (Server)ctx.getUser();
        }

        List<String> command = new LinkedList<>();
        try {
            if(server == null) {
                command.add(config.getString("command"));
            }
            else {
                command.add(Paths.get(server.getLocalPath(), config.getString("command")).toString());
            }

            JSONArray args = JSONUtils.safeGetArray(config, "arguments");
            if(args != null) {
                for(int i = 0; i < args.length(); ++i) {
                    command.add(args.getString(i));
                }
            }
        }
        catch(JSONException e) {
            log.error("Configuration error", e);
            return false;
        }

        log.info("Running command " + command.get(0));

        XProcess proc = new XProcess(command, ctx.getDestination().toString());

        if(server != null) {
            log.info("Registering process with server " + server.getUuid());
            server.setProcess(proc);
        }

        if(!proc.run()) {
            log.info("Command " + command.get(0) + " failed");
            return false;
        }

        return true;
    }
}