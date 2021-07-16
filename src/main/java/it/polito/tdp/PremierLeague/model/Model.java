package it.polito.tdp.PremierLeague.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model {
	
	PremierLeagueDAO dao;
	private Graph<Player, DefaultWeightedEdge> grafo;
	private Map<Integer, Player> idMap;
	
	
	public Model() {
		
		this.dao = new PremierLeagueDAO ();
		this.idMap = new HashMap <Integer, Player>();
		this.dao.listAllPlayers(idMap);
	
	}

public void creGrafo(Match m) {
	
	grafo = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
	
	// aggiungo i vertici
	
	Graphs.addAllVertices(this.grafo, this.dao.getVertici(m, idMap));
	
	//aggiungo gli archi
	
	for(Adiacenza a: dao.getAdiacenze(m, idMap)) {
		
		if(a.getPeso() >= 0) {
			//p1 meglip di p2
			if(grafo.containsVertex(a.getP1()) &&grafo.containsVertex(a.getP2())) {
				Graphs.addEdgeWithVertices(this.grafo, a.getP1(), a.getP2(), a.getPeso());
			}
		} else {
			//p2 meglio di p1
			if(grafo.containsVertex(a.getP1()) &&grafo.containsVertex(a.getP2())) {
				Graphs.addEdgeWithVertices(this.grafo, a.getP2(), a.getP1(), (-1) * a.getPeso());
			}
			
		}
	}
}

public int nVertici () {
	return this.grafo.vertexSet().size();
	
}


public int nArchi() {
	
	return this.grafo.edgeSet().size();
	
}



public Graph<Player, DefaultWeightedEdge> getGrafo() {
	
	return this.grafo;
}

public List<Match> getTuttiMatch () {
	List<Match> matches = dao.listAllMatches();
	Collections.sort(matches, new Comparator <Match>(){
		public int compare(Match o1, Match o2) {
			
			return o1.getMatchID().compareTo(o2.matchID);
		}
	});
	
	return matches;
	
}

public GiocatoreMigliore getMigliore() {
	if (grafo == null) {
		return null;
	}
	
	Player best = null;
	Double maxDelta = (double) Integer.MIN_VALUE;
	
	for(Player p: this.grafo.vertexSet()) {
		
		// calcolo la somma dei pesi degli archi uscenti
		double pesoUscente = 0.0;
		for(DefaultWeightedEdge edge: this.grafo.outgoingEdgesOf(p)) {
			pesoUscente += this.grafo.getEdgeWeight(edge);
		}
		
		// calcolo la somma degli archi entranti
		double PesoEntrante = 0.0;
		for(DefaultWeightedEdge edge: this.grafo.incomingEdgesOf(p)) {
			
			PesoEntrante += this.grafo.getEdgeWeight(edge);
		}
		
		double delta = pesoUscente - PesoEntrante;
		if(delta > maxDelta) {
			best = p;
			maxDelta = delta;
		}
	}
	
	return new GiocatoreMigliore (best, maxDelta);
}




}


