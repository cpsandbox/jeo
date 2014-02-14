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
package org.jeo.ogr;

import java.io.File;

import org.jeo.Tests;
import org.jeo.data.VectorApiTestBase;
import org.jeo.data.VectorDataset;
import org.junit.BeforeClass;

public class SpatiaLiteApiTest extends VectorApiTestBase {

    @BeforeClass
    public static void initOGR() {
        OGRTest.initOGR();
    }

    @Override
    protected VectorDataset createVectorData() throws Exception {
        File dir = Tests.newTmpDir("spatialite", "data");
        Tests.unzip(getClass().getResourceAsStream("usa.db.zip"), dir);

        OGRWorkspace ws = SpatiaLite.open(new File(dir, "usa.db"));
        return ws.get("states");
    }

}
