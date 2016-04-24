package edu.cmu.ece18549.little_brother.littlebrother;

import java.util.HashMap;
import java.util.List;

/**
 * Created by alexmaeda on 4/24/16.
 */
public interface DataImporter {

    public void importData(List<String> devices,
                           HashMap<String, List<String>> deviceDetails);

}
