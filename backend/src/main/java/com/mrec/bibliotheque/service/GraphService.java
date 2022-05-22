package com.mrec.bibliotheque.service;

import com.mrec.bibliotheque.model.Book;
import com.mrec.bibliotheque.model.Neighbor;
import com.mrec.bibliotheque.model.Scoring;
import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.util.mxCellRenderer;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableUndirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

@Service
public class GraphService {
    ListenableUndirectedGraph<String, DefaultEdge> graph;
    public GraphService(){
        this.graph = new ListenableUndirectedGraph<>(DefaultEdge.class);
    }

    public void createGraphByBookList(List<Book> bookList) throws IOException {
        File imgFile = new File("./grapha.png");
        imgFile.createNewFile();

        for (Book book : bookList) {
            graph.addVertex(book.getId());
        }
        for (int i = 0; i < bookList.size(); i++) {
            for (int j = i + 1; j < bookList.size(); j++) {
                var result = BibliothequeService.distanceJ(bookList.get(i), bookList.get(j));
                System.out.println("Resultat pour i : " + i + " et j : " + j + " = " + result);

                if (result < 0.7) {
                    graph.addEdge(bookList.get(i).getId(), bookList.get(j).getId());
                }
            }
        }
        DijkstraShortestPath<String, DefaultEdge> dijkstraShortestPath
                = new DijkstraShortestPath<>(graph);
        List<String> shortestPath = dijkstraShortestPath
                .getPath(bookList.get(0).getId(), bookList.get(1).getId()).getVertexList();

        System.out.println("SHORTEST PATH : " + shortestPath);
        //TO draw graph
        JGraphXAdapter<String, DefaultEdge> graphAdapter =
                new JGraphXAdapter<>(graph);
        mxIGraphLayout layout = new mxCircleLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());


        BufferedImage image =
                mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
        imgFile = new File("grapha.png");
        ImageIO.write(image, "PNG", imgFile);
    }

    public HashMap mapOfScores(List<Book> bookList){
        DijkstraShortestPath<String, DefaultEdge> dijkstraShortestPath
                = new DijkstraShortestPath<>(graph);


        DepthFirstIterator<String, DefaultEdge> depthFirstIterator
                = new DepthFirstIterator<>(graph);
        int n = bookList.size() - 1;
        HashMap<String, Double> hashMap = new HashMap<>();
        Double valueOfSum = 0.0;
        Double clossnessCentrality = 0.0;

        for(Book book : bookList) {

            while (depthFirstIterator.hasNext()) {
                System.out.println("Book : " + book.getId());
                String valueOfNode = depthFirstIterator.next();
                System.out.println("DepthFirstIterator : " + valueOfNode);

                if (!Objects.equals(book.getId(), valueOfNode)) {
                    if(dijkstraShortestPath.getPath(book.getId(), valueOfNode) !=null)
                        valueOfSum += dijkstraShortestPath
                                .getPath(book.getId(), valueOfNode).getVertexList().size() - 1;
                }
            }
            if(valueOfSum != 0.0) {
                clossnessCentrality = n / valueOfSum;
            }
            hashMap.put(book.getId(), clossnessCentrality);
            valueOfSum = 0.0;
            clossnessCentrality = 0.0;
            depthFirstIterator
                    = new DepthFirstIterator<>(graph);
        }
        return hashMap;
    }

    public List<String> getNeighbor(String nodeId){
        return Graphs.neighborListOf(graph, nodeId);
    }

    public Map<String, List<String>> getNeighborAll(List<Book> bookList){
        Map<String, List<String>> hashMap = new HashMap<String, List<String>>();
        for (Book book: bookList) {
            List<String> neighbor =  getNeighbor(book.getId());
            hashMap.put(book.getId(), neighbor);
        }
        return hashMap;
    }


    public ListenableUndirectedGraph<String, DefaultEdge> getGraph() {
        return graph;
    }

}
