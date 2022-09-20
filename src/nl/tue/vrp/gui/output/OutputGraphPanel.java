package nl.tue.vrp.gui.output;

import javax.swing.*;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.StaticLayoutAlgorithm;
import edu.uci.ics.jung.visualization.*;
import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.renderers.BasicNodeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.BasicNodeRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.visualization.util.NodeShapeFactory;
import nl.tue.vrp.gui.util.Pair;
import nl.tue.vrp.output.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.function.Function;

public class OutputGraphPanel extends JPanel {
    private final ScalingControl scaler;

    // posScale is needed to fill the graph within the window and show the correct tooltip label.
    private static final double posScale = 7;
    private final VisualizationViewer<Integer, String> vv;
    private final Network<Integer, String> graph;
    private final Map<Integer, Point2D> map;
    private final Map<Integer, NodeOutput> node_map;
    private final SelectedItems selectedItems;

    private final double offsetX, offsetY;

    public OutputGraphPanel(Output output) {
        setLayout(new BorderLayout());
        node_map = buildMap(output);
        graph = buildGraph(node_map);
        selectedItems = new SelectedItems();

        scaler = new CrossoverScalingControl();
        map = new HashMap<>();
        LayoutAlgorithm<Integer> layoutAlgorithm = new StaticLayoutAlgorithm<>();
        offsetX = -getMinX(node_map);
        offsetY = -getMinY(node_map);
        VisualizationModel<Integer, String> model =
                new BaseVisualizationModel<>(
                        graph, layoutAlgorithm, e -> {
                            java.awt.Point pos = node_map.get(e).getLocation();
                    return Point.of((pos.getX() + offsetX) * posScale, (pos.getY() + offsetY) * posScale);
                }, calculateDimension(node_map));
        vv = new VisualizationViewer<>(model, new Dimension(500, 300));

        // TODO: Add background cartesian
//        ImageIcon icon = new
//        if (icon != null) {
//            vv.addPreRenderPaintable(
//                    new VisualizationViewer.Paintable() {
//                        public void paint(Graphics g) {
//                            Graphics2D g2d = (Graphics2D) g;
//                            AffineTransform oldXform = g2d.getTransform();
//                            AffineTransform lat =
//                                    vv.getRenderContext()
//                                            .getMultiLayerTransformer()
//                                            .getTransformer(MultiLayerTransformer.Layer.LAYOUT)
//                                            .getTransform();
//                            AffineTransform vat =
//                                    vv.getRenderContext()
//                                            .getMultiLayerTransformer()
//                                            .getTransformer(MultiLayerTransformer.Layer.VIEW)
//                                            .getTransform();
//                            AffineTransform at = new AffineTransform();
//                            at.concatenate(g2d.getTransform());
//                            at.concatenate(vat);
//                            at.concatenate(lat);
//                            g2d.setTransform(at);
//                            g.drawImage(icon.getImage(), 0, 0, icon.getIconWidth(), icon.getIconHeight(), vv);
//                            g2d.setTransform(oldXform);
//                        }
//
//                        public boolean useTransform() {
//                            return false;
//                        }
//                    });
//        }

        vv.getRenderer()
                .setNodeRenderer( new BasicNodeRenderer<>());
//                        new GradientNodeRenderer<>(vv, Color.white, Color.red, Color.white, Color.blue, false));
        vv.getRenderContext().setLabelOffset(0);
        NodeShapeFactory<Integer> factory = new NodeShapeFactory<>();
        vv.getRenderContext().setNodeShapeFunction(e -> {
            NodeOutput nd = node_map.get(e);
            Shape shape = null;
            if (nd instanceof DepotOutput) {
                shape = factory.getRegularPolygon(e, 5);
            } else if (nd instanceof SatelliteOutput) {
                shape = factory.getRegularPolygon(e, 4);
            } else {
                shape = factory.getEllipse(e);
            }
            return AffineTransform.getScaleInstance(2, 2).createTransformedShape(shape);
        });
        vv.getRenderContext().setNodeFillPaintFunction(e -> {
            if (selectedItems.containsNode(e)) {
                return Color.CYAN;
            }
            return Color.PINK;
        });

        vv.getRenderContext().setEdgeDrawPaintFunction(edge -> {
            EndpointPair<Integer> pair = graph.incidentNodes(edge);
//            System.out.println(pair);
            if (selectedItems.containsEdge(pair.nodeU(), pair.nodeV())) {
//                System.out.println("true");
                return Color.BLUE;
            }
            return Color.BLACK;
        });


        // add my listeners for ToolTips
        vv.setNodeToolTipFunction(e -> {
            NodeOutput node = node_map.get(e);
            if (node instanceof CustomerOutput) {
                CustomerOutput cust = (CustomerOutput) node;
                return String.format("C%d (%.1f, %.1f): %s %d", e, cust.getLocation().getX(), cust.getLocation().getY(), cust.getType(), cust.getDemand());
            } else if (node instanceof DepotOutput) {
                return String.format("D%d (%.1f, %.1f)", e, node.getLocation().getX(), node.getLocation().getY());
            } else {
                return String.format("S%d (%.1f, %.1f)", e, node.getLocation().getX(), node.getLocation().getY());
            }
        });
        vv.setEdgeToolTipFunction(edge -> {
            EndpointPair<Integer> pair = graph.incidentNodes(edge);
            double distance = node_map.get(pair.nodeV()).getLocation().distance(node_map.get(pair.nodeU()).getLocation());
            return String.format("E%s: %.2f", pair, distance);
        });

        vv.getRenderContext().setNodeLabelFunction(String::valueOf);
        vv.getRenderer()
                .getNodeLabelRenderer()
                .setPositioner(new BasicNodeLabelRenderer.InsidePositioner());
        vv.getRenderer().getNodeLabelRenderer().setPosition(Renderer.NodeLabel.Position.CNTR);
        vv.getRenderContext().setEdgeLabelFunction(edge -> {
            EndpointPair<Integer> pair = graph.incidentNodes(edge);
            double distance = node_map.get(pair.nodeV()).getLocation().distance(node_map.get(pair.nodeU()).getLocation());
            return String.format("%.2f", distance);
        });

        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        add(panel);
        final AbstractModalGraphMouse graphMouse = new DefaultModalGraphMouse<String, Number>();
        vv.setGraphMouse(graphMouse);

        vv.addKeyListener(graphMouse.getModeKeyListener());
//        vv.setToolTipText("<html><center>Type 'p' for Pick mode<p>Type 't' for Transform mode");

        JButton plus = new JButton("+");
        plus.addActionListener(e -> {
            scaler.scale(vv, 1.1f, vv.getCenter());
        });
        JButton minus = new JButton("-");
        minus.addActionListener(e -> {
            scaler.scale(vv, 1/1.1f, vv.getCenter());
        });

        JButton reset = new JButton("reset");
        reset.addActionListener(
                e -> {
                    vv.getRenderContext()
                            .getMultiLayerTransformer()
                            .getTransformer(MultiLayerTransformer.Layer.LAYOUT)
                            .setToIdentity();
                    vv.getRenderContext()
                            .getMultiLayerTransformer()
                            .getTransformer(MultiLayerTransformer.Layer.VIEW)
                            .setToIdentity();
                });

        JPanel controls = new JPanel();
        controls.add(plus);
        controls.add(minus);
        controls.add(reset);
        add(controls, BorderLayout.SOUTH);
    }

    private Map<Integer, NodeOutput> buildMap(Output output) {
        Map<Integer, NodeOutput> map = new HashMap<>();
        for (CustomerOutput c : output.getCustomers()) {
            map.put(c.getId(), c);
        }
        for (SatelliteOutput c : output.getSatellites()) {
            map.put(c.getId(), c);
        }
        for (DepotOutput c : output.getDepots()) {
            map.put(c.getId(), c);
        }
        return map;
    }

    private Network<Integer, String> buildGraph(Map<Integer, NodeOutput> map) {
        Set<Pair<Integer, Integer>> set = new TreeSet<>();
        MutableNetwork<Integer, String> graph = NetworkBuilder.directed().allowsParallelEdges(true).allowsSelfLoops(false).build();
        for (Integer nodeId : map.keySet()) {
            graph.addNode(nodeId);
        }
        // TODO: edges
        for (NodeOutput node : map.values()) {
            if (node == null) continue;
            if (node instanceof DepotOutput) {
                DepotOutput depot = (DepotOutput) node;
                if (depot.getRoutes() == null) continue;
                for (RouteOutput route : depot.getRoutes()) {
                    int currID = depot.getId();
                    for (VisitOutput visit : route.getVisits()) {
                        if (visit.getNodeID() != currID) {
                            Pair<Integer, Integer> pair = new Pair<>(visit.getNodeID(), currID);
                            if (set.contains(pair)) continue;
                            set.add(pair);
                            graph.addEdge(currID, visit.getNodeID(), String.format("(%d,%d):%.3f",currID, visit.getNodeID(),visit.getLegCost()));
                            currID = visit.getNodeID();
                        }
                    }
                }
            } else if (node instanceof SatelliteOutput) {
                SatelliteOutput sat = (SatelliteOutput) node;
                if (sat.getRoutes() == null) continue;
                for (RouteOutput route : sat.getRoutes()) {
                    int currID = sat.getId();
                    for (VisitOutput visit : route.getVisits()) {
                        if (visit.getNodeID() != currID) {
                            Pair<Integer, Integer> pair = new Pair<>(visit.getNodeID(), currID);
                            if (set.contains(pair)) continue;
                            set.add(pair);
                            graph.addEdge(currID, visit.getNodeID(), String.format("(%d,%d):%.3f",currID, visit.getNodeID(),visit.getLegCost()));
                            currID = visit.getNodeID();
                        }
                    }
                }
            }
        }
        return graph;
    }

    private Dimension calculateDimension(Map<Integer, NodeOutput> map) {
        double left = Double.MAX_VALUE;
        double right = -Double.MAX_VALUE;
        double top = -Double.MAX_VALUE;
        double bottom = Double.MAX_VALUE;
        for (NodeOutput no : map.values()) {
            left = Math.min(left, no.getLocation().getX());
            right = Math.max(right, no.getLocation().getX());
            top = Math.max(top, no.getLocation().getY());
            bottom = Math.min(bottom, no.getLocation().getY());
        }
//        System.out.printf("left: %.2f, right: %.2f, top: %.2f, bottom: %.2f", left, right, top, bottom);
        return new Dimension((int) Math.ceil(right - left) + 1, (int) Math.ceil(top - bottom) + 1);
    }

    private double getMinX(Map<Integer, NodeOutput> map) {
        double left = Double.MAX_VALUE;
        for (NodeOutput no : map.values()) {
            left = Math.min(left, no.getLocation().getX());
        }
        return left;
    }

    private double getMinY(Map<Integer, NodeOutput> map) {
        double top = Double.MAX_VALUE;
        for (NodeOutput no : map.values()) {
            top = Math.min(top, no.getLocation().getY());
        }
        return top;
    }

    public void highlight(Object object) {
        if (object == null) {
            selectedItems.clear();
        } else if (object instanceof CustomerOutput) {
            CustomerOutput cust = (CustomerOutput) object;
            selectedItems.selectCustomer(cust);
        } else if (object instanceof SatelliteOutput) {
            SatelliteOutput sat = (SatelliteOutput) object;
            selectedItems.selectSatellite(sat);
        } else if (object instanceof DepotOutput) {
            DepotOutput depot = (DepotOutput) object;
            selectedItems.selectDepot(depot);
        } else if (object instanceof RouteOutput) {
            RouteOutput route = (RouteOutput) object;
            selectedItems.selectRoute(route);
        } else {
            System.out.println("highlight ???");
        }
        vv.repaint();
    }

    private static class SelectedItems {
        private final List<Object> objectList;
        private final Set<Integer> node;
        private final Set<Pair<Integer, Integer>> edge;

        public SelectedItems() {
            node = new TreeSet<>();
            edge = new TreeSet<>();
            objectList = new ArrayList<>();
        }

        public void selectCustomer(CustomerOutput cust) {
            objectList.add(cust);
            node.add(cust.getId());
        }

        public void selectSatellite(SatelliteOutput sat) {
            objectList.add(sat);
            node.add(sat.getId());
            if (sat.getRoutes() != null) {
                for (RouteOutput route : sat.getRoutes()) {
                    selectRoute(route);
                }
            }
        }

        public void selectDepot(DepotOutput depot) {
            objectList.add(depot);
            node.add(depot.getId());
            if (depot.getRoutes() != null) {
                for (RouteOutput route : depot.getRoutes()) {
                    selectRoute(route);
                }
            }
        }

        public void selectRoute(RouteOutput route) {
            int prevNode = -1;
            for (VisitOutput visit : route.getVisits()) {
                node.add(visit.getNodeID());
                if (prevNode != -1) {
                    edge.add(new Pair<>(prevNode, visit.getNodeID()));
                }
                prevNode = visit.getNodeID();
            }
        }

        public boolean containsNode(int nodeID) {
            return node.contains(nodeID);
        }

        public boolean containsEdge(int sourceNode, int destNode) {
            return edge.contains(new Pair<>(sourceNode, destNode));
        }

        public void clear() {
            node.clear();
            edge.clear();
            objectList.clear();
        }
    }
}
