package com.mrec.bibliotheque;

import com.mrec.bibliotheque.model.Book;
import com.mrec.bibliotheque.model.TokenDto;
import com.mrec.bibliotheque.service.BibliothequeService;
import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.util.mxCellRenderer;
import lombok.SneakyThrows;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class TestMain {

    @SneakyThrows
    public static void main(String args[]){


        Book book1 = new Book();
        Book book2 = new Book();
        Book book3 = new Book();
        Book book4 = new Book();
        book1.setId("1");
        book2.setId("2");
        book3.setId("3");
        book4.setId("4");
        List<Book> bookList = new ArrayList<>();
        bookList.add(book1);
        bookList.add(book2);
        bookList.add(book3);
        bookList.add(book4);

        List<TokenDto> tokens1 = new ArrayList<>();

        List<TokenDto> tokens2 = new ArrayList<>();
        List<TokenDto> tokens3 = new ArrayList<>();
        List<TokenDto> tokens4 = new ArrayList<>();

        tokens1.add(new TokenDto("0",0, 0));
        tokens1.add(new TokenDto("1",0, 0));
        tokens1.add(new TokenDto("2",0, 0));
        tokens1.add(new TokenDto("5",0, 0));
        tokens1.add(new TokenDto("6",0, 0));
        tokens1.add(new TokenDto("10",0, 0));
        tokens1.add(new TokenDto("11",0, 0));
        tokens1.add(new TokenDto("12",0, 0));

        tokens2.add(new TokenDto("10",0, 0));
        tokens2.add(new TokenDto("11",0, 0));
        tokens2.add(new TokenDto("12",0, 0));
        tokens2.add(new TokenDto("13",0, 0));
        tokens2.add(new TokenDto("14",0, 0));
        tokens2.add(new TokenDto("15",0, 0));
        tokens2.add(new TokenDto("16",0, 0));

        tokens3.add(new TokenDto("10",0, 0));
        tokens3.add(new TokenDto("11",0, 0));
        tokens3.add(new TokenDto("12",0, 0));
        tokens3.add(new TokenDto("10",0, 0));
        tokens3.add(new TokenDto("11",0, 0));
        tokens3.add(new TokenDto("12",0, 0));


        tokens4.add(new TokenDto("100",0, 0));
        tokens4.add(new TokenDto("200",0, 0));

        book1.setToken(tokens1);
        book2.setToken(tokens2);
        book3.setToken(tokens3);
        book4.setToken(tokens4);

        double result = BibliothequeService.distanceJ(book1, book2);

        ListenableUndirectedGraph<String,DefaultEdge> graph = createGraph(bookList);

        System.out.println(mapOfScores(graph,bookList));
    }
    public static HashMap mapOfScores(ListenableUndirectedGraph<String,DefaultEdge> graph,List<Book> bookList){
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
    public static ListenableUndirectedGraph createGraph(List<Book> bookList) throws IOException {
        File imgFile = new File("./grapha.png");
        imgFile.createNewFile();
        ListenableUndirectedGraph<String,DefaultEdge> graph = new ListenableUndirectedGraph<>(DefaultEdge.class);

        for(Book book: bookList){
            graph.addVertex(book.getId());
        }
        for(int i = 0; i < bookList.size(); i++){
            for(int j = i + 1; j < bookList.size(); j++){
                var result = BibliothequeService.distanceJ(bookList.get(i), bookList.get(j));

                if(result<0.7){
                    graph.addEdge(bookList.get(i).getId(),bookList.get(j).getId());
                }
            }
        }



        //TO draw graph
        JGraphXAdapter<String, DefaultEdge> graphAdapter =
                new JGraphXAdapter<>(graph);
        mxIGraphLayout layout = new mxCircleLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());


        BufferedImage image =
                mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
        imgFile = new File("grapha.png");
        ImageIO.write(image, "PNG", imgFile);
        return graph;
    }

}
