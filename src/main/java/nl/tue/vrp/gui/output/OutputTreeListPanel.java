package nl.tue.vrp.gui.output;

import nl.tue.vrp.output.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.function.Function;

public class OutputTreeListPanel extends JPanel {
    private final Function<Object, Void> onSelect;

    OutputTreeListPanel(Output output, Function<Object, Void> onSelect) {
        this.onSelect = onSelect;

        setLayout(new BorderLayout());
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(String.format("root: {satisfied: %b, cost: %.2f, finishTime: %d}", output.getSatisfied(), output.getTotalDistance(), output.getTimeFinished()));
        DefaultMutableTreeNode depotSubtree = new DefaultMutableTreeNode("depot");
        root.add(depotSubtree);
        for (DepotOutput depot : output.getDepots()) {
            double totalDistance = 0;
            int finishTime = 0;
            for (RouteOutput route : depot.getRoutes()) {
                VisitOutput lastNode = route.getVisits().get(route.getVisits().size() - 1);
                totalDistance += lastNode.getAccumulatedCost();
                finishTime = Math.max(finishTime, lastNode.getDepartureTime());
            }
            DefaultMutableTreeNode dnode = new DefaultMutableTreeNode(new TreeNodeData(String.format("%d (%.0f,%.0f): {cost: %.2f, finishTime: %d}", depot.getId(), depot.getLocation().getX(), depot.getLocation().getY(), totalDistance, finishTime), depot));
            depotSubtree.add(dnode);
            for (VehicleOutput vehicle : depot.getVehicles()) {
                DefaultMutableTreeNode vnode = new DefaultMutableTreeNode(new TreeNodeData(String.format("Vehicle %d", vehicle.getId()), vehicle));
                dnode.add(vnode);
            }
        }

        DefaultMutableTreeNode satelliteSubtree = new DefaultMutableTreeNode("satellite");
        root.add(satelliteSubtree);
        for (SatelliteOutput sat : output.getSatellites()) {
            double totalDistance = 0;
            int finishTime = 0;
            for (RouteOutput route : sat.getRoutes()) {
                VisitOutput lastNode = route.getVisits().get(route.getVisits().size() - 1);
                totalDistance += lastNode.getAccumulatedCost();
                finishTime = Math.max(finishTime, lastNode.getDepartureTime());
            }
            DefaultMutableTreeNode snode = new DefaultMutableTreeNode(new TreeNodeData(String.format("%d (%.0f,%.0f): {cost: %.2f, finishTime: %d}", sat.getId(), sat.getLocation().getX(), sat.getLocation().getY(), totalDistance, finishTime), sat));
            satelliteSubtree.add(snode);
            for (VehicleOutput vehicle : sat.getVehicles()) {
                DefaultMutableTreeNode vnode = new DefaultMutableTreeNode(new TreeNodeData(String.format("Vehicle %d", vehicle.getId()), vehicle));
                snode.add(vnode);
            }
        }

        DefaultMutableTreeNode custSubtree = new DefaultMutableTreeNode("customer");
        root.add(custSubtree);
        for (CustomerOutput cus : output.getCustomers()) {
            DefaultMutableTreeNode cnode = new DefaultMutableTreeNode(new TreeNodeData(String.format("%d (%.0f,%.0f): %s %d", cus.getId(), cus.getLocation().getX(), cus.getLocation().getY(), cus.getType(), cus.getDemand()), cus));
            custSubtree.add(cnode);
        }

        JTree jt = new JTree(root);
        removeTreeIcon(jt);

        jt.addTreeSelectionListener(e -> {
            this.onSelect.apply(null);
            TreePath path = e.getPath();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node.getUserObject() instanceof TreeNodeData nd) {
                this.onSelect.apply(nd.getData());
            }
        });
        for (int i = 0; i < jt.getRowCount(); ++i) {
            jt.expandRow(i);
        }

        add(jt);
    }

    private static void removeTreeIcon(JTree jt) {
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) jt.getCellRenderer();
        renderer.setLeafIcon(null);
        renderer.setClosedIcon(null);
        renderer.setOpenIcon(null);
    }

    private static class TreeNodeData {
        private final String label;
        private final Object data;

        TreeNodeData(String label, Object data) {
            this.label = label;
            this.data = data;
        }

        @Override
        public String toString() {
            return label;
        }

        public boolean isNode() {
            return data instanceof NodeOutput;
        }

        public String getLabel() {
            return label;
        }

        public Object getData() {
            return data;
        }
    }
}
