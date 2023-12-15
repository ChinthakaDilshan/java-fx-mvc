package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import dto.CustomerDto;
import dto.ItemDto;
import dto.tm.CustomerTm;
import dto.tm.ItemTm;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.ItemModel;
import model.impl.ItemModelImpl;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.function.Predicate;

public class ItemFormController {
    public BorderPane pane;
    public JFXTextField txtCode;
    public JFXTextField txtDescription;
    public JFXTextField txtUnitPrice;
    public JFXTextField txtQty;
    public JFXTextField txtSearch;
    public JFXTreeTableView<ItemTm> tblItem;
    public TreeTableColumn colCode;
    public TreeTableColumn colDesc;
    public TreeTableColumn colUnitPrice;
    public TreeTableColumn colQty;
    public TreeTableColumn colOption;

    private ItemModel itemModel = new ItemModelImpl();

    public void initialize(){
        colCode.setCellValueFactory(new TreeItemPropertyValueFactory<>("code"));
        colDesc.setCellValueFactory(new TreeItemPropertyValueFactory<>("desc"));
        colUnitPrice.setCellValueFactory(new TreeItemPropertyValueFactory<>("unitPrice"));
        colQty.setCellValueFactory(new TreeItemPropertyValueFactory<>("qty"));
        colOption.setCellValueFactory(new TreeItemPropertyValueFactory<>("btn"));
        loadItemTable();

        txtSearch.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                tblItem.setPredicate(new Predicate<TreeItem<ItemTm>>() {
                    @Override
                    public boolean test(TreeItem<ItemTm> treeItem) {
                        return treeItem.getValue().getCode().toLowerCase().contains(newValue.toLowerCase()) ||
                                treeItem.getValue().getDesc().toLowerCase().contains(newValue.toLowerCase());

                    }
                });
            }
        });
    }

    private void loadItemTable() {
        ObservableList<ItemTm> tmList = FXCollections.observableArrayList();

        try {
            List<ItemDto> dtoList  = itemModel.allItems();
            for (ItemDto dto:dtoList) {
                JFXButton btn = new JFXButton("Delete");

                ItemTm tm = new ItemTm(
                        dto.getCode(),
                        dto.getDesc(),
                        dto.getUnitPrice(),
                        dto.getQty(),
                        btn
                );

                btn.setOnAction(actionEvent -> {
                    deleteItem(tm.getCode());
                });

                tmList.add(tm);
            }
            RecursiveTreeItem<ItemTm> treeItem = new RecursiveTreeItem<>(tmList, RecursiveTreeObject::getChildren);
            tblItem.setRoot(treeItem);
            tblItem.setShowRoot(false);
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteItem(String code) {

        try {
            boolean isDeleted = itemModel.deleteItem(code);
            if (isDeleted){
                new Alert(Alert.AlertType.INFORMATION,"Item Deleted!").show();
                loadItemTable();
            }else{
                new Alert(Alert.AlertType.ERROR,"Something went wrong!").show();
            }

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void backButtonOnAction(ActionEvent actionEvent) {
        Stage stage = (Stage) pane.getScene().getWindow();
        try {
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/view/DashboardForm.fxml"))));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void clearFields() {
        tblItem.refresh();
        txtQty.clear();
        txtUnitPrice.clear();
        txtDescription.clear();
        txtCode.clear();
        txtCode.setEditable(true);
    }
    public void saveButtonOnAction(ActionEvent actionEvent) throws SQLException, ClassNotFoundException {
            ItemDto dto = new ItemDto(txtCode.getText(),
                    txtDescription.getText(),
                    Double.parseDouble(txtUnitPrice.getText()),
                    Integer.parseInt(txtQty.getText())
            );
            boolean isSaved = itemModel.saveItem(dto);
            if (isSaved){
                new Alert(Alert.AlertType.INFORMATION,"Item Saved!").show();
                loadItemTable();
                clearFields();
            }


        }

    public void updateButtonOnAction(ActionEvent actionEvent) {
        ItemDto dto = new ItemDto(txtCode.getText(),
                txtDescription.getText(),
                Double.parseDouble(txtUnitPrice.getText()),
                Integer.parseInt(txtQty.getText())
        );

        try {
            boolean isUpdated = itemModel.updateItem(dto);
            if (isUpdated){
                new Alert(Alert.AlertType.INFORMATION,"Item "+dto.getCode()+" Updated!").show();
                loadItemTable();
                clearFields();
            }

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
}
