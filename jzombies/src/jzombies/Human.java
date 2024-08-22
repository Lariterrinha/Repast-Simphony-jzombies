package jzombies;

import java.util.List;

import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.SimUtilities;

/* 
 * The basic behavior for a Human is to react when a Zombie comes within its local
	neighborhood by running away from the area with the most Zombies. Additionally, Humans
	have a certain amount of energy that is expended in running away. If this energy is 0 or
	less then a Human is unable to run
 */

public class Human {
	private ContinuousSpace <Object>space;
	private Grid <Object>grid;
	private int energy, startingEnergy;
	
	// Construtor
	public Human(ContinuousSpace <Object>space, Grid<Object>grid, int energy){
		this.space = space;
		this.grid = grid;
		this.energy = startingEnergy = energy;
	}
	
	/* Query "Observador" que vai ativar o metodo "run" quando a variavel "moved" (do Zombie) for alterada
	 * O zumbi movido tambem prescisa estar na vizinhança (Moore/8 celulas grade adjacentes)
	 */
	@Watch( watcheeClassName = "jzombies.Zombie",
			watcheeFieldNames = "moved",
			query = "within_moore 1",
			whenToTrigger = WatcherTriggerSchedule.IMMEDIATE) 
	public void run () {
		// get the grid location of this Human
		GridPoint pt = grid.getLocation(this);
		
		// use the GridCellNgh class to create GridCells for
		// the surrounding neighborhood .
		GridCellNgh<Zombie>nghCreator = new GridCellNgh <Zombie>(grid,pt,
				Zombie.class,1,1);
		List <GridCell<Zombie>>gridCells = nghCreator.getNeighborhood(true);
		SimUtilities.shuffle(gridCells, RandomHelper.getUniform ());

		GridPoint pointWithLeastZombies = null ;
		int minCount = Integer.MAX_VALUE ;
		for ( GridCell < Zombie > cell : gridCells ) {
			if ( cell . size () < minCount ) {
				pointWithLeastZombies = cell . getPoint ();
				minCount = cell . size ();
			}
		}

		if (energy > 0) {
			moveTowards (pointWithLeastZombies);
		} else {
			energy = startingEnergy ;
		}
	}
	public void moveTowards (GridPoint pt) {
		// only move if we are not already in this grid location
		if (!pt.equals (grid.getLocation (this))) {
			NdPoint myPoint = space.getLocation(this);
			NdPoint otherPoint = new NdPoint ( pt.getX() , pt.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint,
					otherPoint );
			
			// Move 2 "passos" na direção desejada
			space.moveByVector(this,2,angle, 0);
			myPoint = space.getLocation(this);
			grid.moveTo(this, (int)myPoint.getX() , (int)myPoint.getY());
			energy--;
		}
	}

}
