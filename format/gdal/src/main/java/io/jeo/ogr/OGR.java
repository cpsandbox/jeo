/* Copyright 2014 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jeo.ogr;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gdal.ogr.DataSource;
import org.gdal.ogr.Driver;
import org.gdal.ogr.ogr;
import io.jeo.data.Disposable;
import io.jeo.vector.FileVectorDriver;
import io.jeo.vector.VectorDriver;
import io.jeo.vector.Schema;
import io.jeo.util.Key;
import io.jeo.util.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.jeo.util.Util.set;

/**
 * Driver for formats supported by the OGR/GDAL library.
 * <p>
 * Usage:
 * <pre><code>
 * Workspace ws = OGR.open('states.csv');
 * </code></pre>
 * </p>
 *
 * @author Justin Deoliveira, Boundless
 */
public class OGR extends FileVectorDriver<OGRWorkspace> implements Disposable {

    public static final Key<String> DRIVER = new Key<String>("driver", String.class);

    static final Logger LOG = LoggerFactory.getLogger(OGR.class);

    public static void init() throws Throwable {
        if (ogr.GetDriverCount() == 0) {
            ogr.RegisterAll();
        }
    }

    static {
        try {
            init();
        }
        catch(Throwable e) {
            LOG.debug("gdal initialization failed", e);
        }
    }

    public static OGRWorkspace open(Path path) throws IOException {
        return new OGR().open(path.toFile(), null);
    }

    org.gdal.ogr.Driver ogrDrv;

    public OGR() {
    }

    public OGR(org.gdal.ogr.Driver ogrDrv) {
        this.ogrDrv = ogrDrv;
    }

    @Override
    public boolean isEnabled(Messages messages) {
        try {
            init();
            return true;
        }
        catch(Throwable t) {
            Messages.of(messages).report(t);
            return false;
        }
    }

    @Override
    public void close() {
        if (ogrDrv != null) {
            ogrDrv.delete();
            ogrDrv = null;
        }
    }

    @Override
    public String name() {
        return ogrDrv != null ? ogrDrv.GetName() : "OGR";
    }

    @Override
    public Class<OGRWorkspace> type() {
        return OGRWorkspace.class;
    }

    @Override
    public List<Key<?>> keys() {
        return (List) Arrays.asList(FILE, DRIVER);
    }

    @Override
    public String family() {
        return "gdal";
    }

    @Override
    protected boolean canCreate(File file, Map<?, Object> opts, Messages msgs) {
        return false;
    }

    @Override
    protected OGRWorkspace create(File file, Map<?, Object> opts, Schema schema) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canOpen(Map<?, Object> opts, Messages msgs) {
        try {
            init();
        }
        catch(Throwable t) {
            Messages.of(msgs).report(t);
            return false;
        }

        return super.canOpen(opts, msgs);
    }

    @Override
    protected boolean canOpen(File file, Map<?, Object> opts, Messages msgs) {
        msgs = Messages.of(msgs);

        if (DRIVER.in(opts)) {
            String drvName = DRIVER.get(opts);
            Driver drv = ogr.GetDriverByName(drvName);
            if (drv == null) {
                msgs.report("Unknown driver: " + drvName);
                return false;
            }

            try {
                DataSource data = drv.Open(file.getAbsolutePath());
                if (data == null) {
                    msgs.report("Driver: " + drvName + " unable to open file: " + file);
                    return false;
                }
            }
            catch(Exception e) {
                msgs.report(e);
                return false;
            }
        }

        return true;
    }

    @Override
    protected OGRWorkspace open(File file, Map<?, Object> opts) throws IOException {
        // was driver explicitly specified
        if (DRIVER.in(opts)) {
            Driver drv = ogr.GetDriverByName(DRIVER.get(opts));
            return new OGRWorkspace(file, new OGR(drv));
        }

        DataSource ds = ogr.OpenShared(file.getAbsolutePath());
        if (ds == null) {
            throw new IOException("Unable to open file: " + file);
        }

        try {
            return new OGRWorkspace(file, new OGR(ds.GetDriver()));
        }
        finally {
            ds.delete();
        }
    }

    static final Set<Capability> CAPABILITIES = set(BOUND);

    @Override
    public Set<Capability> capabilities() {
        return CAPABILITIES;
    }
}
