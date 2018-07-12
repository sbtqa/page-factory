/*
 * Copyright 2018 sbt-sidochenko-vv.
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

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

/**
 *
 * @author sbt-sidochenko-vv
 */
public class NewEmptyJUnitTest {

    @Test
    public void hello() {
        String test = "Hello ${name}";
        String test1 = "Hello #Data${user.name1} dfdfd";
        String test2 = "Hello #Data${user.name2} sdfsd fsd #Data${user.name3} sdfsdfsd";
        String test3 = "Hello #Data${}";

        String[] tests = {
            test,
            test1,
            test2,
            test3
        };

        System.out.println(test);

    }

   
}
