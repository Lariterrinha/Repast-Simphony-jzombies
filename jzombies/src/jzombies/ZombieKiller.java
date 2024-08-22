package jzombies;

import java.util.ArrayList;
import java.util.List;

//import com.jogamp.nativewindow.awt.AWTPrintLifecycle.Context;


import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;
import repast.simphony.context.Context;

public class ZombieKiller {
	
	private ContinuousSpace<Object> space;  // Cordenadas no 3D de numeros em ponto flutuante
	private Grid<Object> grid;			 	// Grade, que permite ver quem esta por perto, usa inteiros
	private boolean moved;
	private int kills;
	
	// Construtor
	public ZombieKiller(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
		kills = 0;
	}
	
	// Passo (acontece a cada interação da simulação para cada zumbi) por causa do @ScheduledMethod
	@ScheduledMethod(start=1, interval=1)
	public void step() {
		// Get the grid location of this Zombie
		GridPoint pt = grid.getLocation(this);
		
		// Use the GridCellNgh class to create GridCells for the surrounding neighborhood
		// Parametros:grade atual de itens || o ponto em que se busca os vizinhos || Tipo de vizinhos (classe) || dimenção || dimenção
		GridCellNgh<Zombie> nghCreator = new GridCellNgh<Zombie>(grid, pt, Zombie.class,1,1);
		
		// Import repast.simphony.query.space.grid.GridCell
		// Lista da celulas de grade que contem a celula central onde o Zombie está atualmente
		List<GridCell<Zombie>> gridCells = nghCreator.getNeighborhood(true);
		
		// Embaralhar (sem isso o zumbi só movem em uma direção se a quantidade de humanos nas grades forem as mesmas)
		SimUtilities.shuffle(gridCells,RandomHelper.getUniform ());
		
		//Procura o ponto da grade com mais humanos
		GridPoint pointWithMostZombie = null ;
		int maxCount = -1;
		for ( GridCell < Zombie > cell : gridCells ) {
			if ( cell . size () > maxCount ) {
				pointWithMostZombie = cell.getPoint();
				maxCount = cell.size();
			}
		}
		moveTowards(pointWithMostZombie);
		kill();
	}
	
	// Move este zumbi para um ponto
	public void moveTowards (GridPoint pt) {
		//only move if we are not already in this grid location
		if (!pt.equals(grid.getLocation(this))) {
			
			//NdPoint (double, ContinouousSpace)
			NdPoint myPoint = space.getLocation(this);
			NdPoint otherPoint = new NdPoint(pt.getX(),pt.getY());
			double angle = SpatialMath.calcAngleFor2DMovement (space,myPoint,otherPoint );
			space.moveByVector (this,2,angle,0);
			myPoint = space.getLocation(this);
			
			//Gridpoint(int, Grid)
			grid.moveTo(this,(int)myPoint.getX(),(int)myPoint.getY());
			
			moved = true;
			
		}
	}
	
	public void kill () {
		GridPoint pt = grid.getLocation(this);
		List <Object> zombie = new ArrayList <Object>();
		
		// Vê se tem algum humano na grade do zumbi, se sim coloca num arraylist
		for (Object obj: grid.getObjectsAt(pt.getX(), pt.getY())) {
			if (obj instanceof Zombie) {
				zombie.add(obj);
			}
		}
		
		// Se tiver humanos no arraylist transforma um em zumbi
		if (zombie.size() > 0) {
			
			// Escolhe um dos humanos caso tenha mais de um
			int index = RandomHelper.nextIntFromTo(0, zombie.size() - 1);
			Object obj = zombie.get(index);
			
			// Pega a localização e contexto do humano e apaga ele
			NdPoint spacePt = space.getLocation(obj);
			Context<Object> context = ContextUtils.getContext(obj);
			context.remove(obj);
			kills++;
			
		}
	}
	
}
