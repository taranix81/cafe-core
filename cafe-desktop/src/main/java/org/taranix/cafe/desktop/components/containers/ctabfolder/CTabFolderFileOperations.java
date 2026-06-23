package org.taranix.cafe.desktop.components.containers.ctabfolder;

public interface CTabFolderFileOperations {

    void newFile(CTabFolderContainer container);

    void save(CTabFolderContainer container);

    void open(CTabFolderContainer container);

    void saveAs(CTabFolderContainer container);

    void saveAll(CTabFolderContainer container);
}
