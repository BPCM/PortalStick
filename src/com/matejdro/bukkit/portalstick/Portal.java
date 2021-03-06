package com.matejdro.bukkit.portalstick;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.matejdro.bukkit.portalstick.util.BlockUtil;
import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.RegionSetting;
import com.matejdro.bukkit.portalstick.util.Util;

public class Portal {
	private Location teleport;
	private HashSet<Block> border;
	private HashSet<Block> inside;
	private HashSet<Block> behind;
	private boolean vertical;
	private Block centerBlock;
	private User owner;
	private Boolean orange = false;
	private Boolean open = false;
	private boolean disabled = false;
	private boolean transmitter = false;
	private boolean placetorch = false;
	BlockFace teleportFace;
	private HashSet<Location> awayBlocks = new HashSet<Location>();
	private HashMap<Location, String> oldBlocks = new HashMap<Location, String>();
	
	public Portal()
	{
		border = new HashSet<Block>();
		inside = new HashSet<Block>();
	}
	
	public Portal(Location Teleport, Block CenterBlock, HashSet<Block> Border, HashSet<Block> Inside, HashSet<Block> Behind, User Owner, Boolean Orange, Boolean Vertical, BlockFace Teleportface)
	{
		teleport = Teleport;
		border = Border;
		inside = Inside;
		orange = Orange;
		owner = Owner;
		vertical = Vertical;
		teleportFace = Teleportface;
		behind = Behind;
		centerBlock = CenterBlock;
	}
	
	public void delete()
	{
		if (orange != null && owner != null) {
			for (Block b: border)
			{
				if (oldBlocks.containsKey(b.getLocation()))
					BlockUtil.setBlockData(b, oldBlocks.get(b.getLocation()));
				PortalManager.borderBlocks.remove(b.getLocation());
			}
			for (Block b: inside)
			{
				if (oldBlocks.containsKey(b.getLocation()))
					BlockUtil.setBlockData(b, oldBlocks.get(b.getLocation()));
				PortalManager.insideBlocks.remove(b.getLocation());
			}
			if (Config.FillPortalBack > -1)
			{
				for (Block b: behind)
				{
					if (oldBlocks.containsKey(b.getLocation()))
						BlockUtil.setBlockData(b, oldBlocks.get(b.getLocation()));
					PortalManager.behindBlocks.remove(b.getLocation());
				}
			}
			for (Location l : awayBlocks)
			{
				PortalManager.awayBlocksGeneral.remove(l);
				PortalManager.awayBlocksX.remove(l);
				PortalManager.awayBlocksY.remove(l);
				PortalManager.awayBlocksZ.remove(l);
			}
			
			
			if (orange)
			{
				owner.setOrangePortal(null);
			}
			else
			{
				owner.setBluePortal(null);
			}
			
			open = false;
			
			PortalManager.portals.remove(this);
			RegionManager.getRegion(centerBlock.getLocation()).portals.remove(this);
						
	    	if (getDestination() != null && getDestination().isOpen())
	    	{
	    		if (getDestination().isRegionPortal())
	    		{
					RegionManager.getRegion(getDestination().getCenterBlock().getLocation()).regionPortalClosed(orange);
	    		}
	    		else
	    		{
	    			getDestination().close();
	    		}
	    	}
	    		    	
    		if (isRegionPortal())
    			RegionManager.getRegion(centerBlock.getLocation()).regionPortalDeleted(this);
	    		
		}				
	}
	
	public void open()
	{
		Region region = RegionManager.getRegion(((Block)inside.toArray()[0]).getLocation());
	
		for (Block b: inside)
    	{
			b.setType(Material.AIR); 
			
			if (region.getBoolean(RegionSetting.ENABLE_REDSTONE_TRANSFER))
			 {			 				 
				 for (int i = 0; i < 4; i++)
				 {
					 BlockFace face = BlockFace.values()[i];
					 if (b.getRelative(face).getBlockPower() > 0) 
						 {						 
						 	Portal destination = getDestination();
						 	if (destination == null || destination.isTransmitter()) continue;
						 
						 		setTransmitter(true);
						 		if (destination.isOpen())
							 		for (Block b2: destination.getInside())
							 			b2.setType(Material.REDSTONE_TORCH_ON);

						 		else
						 			destination.setPlaceTorch(true);
						 }
				 }
			 }

    	}
		
		if (placetorch)
		{
			((Block)inside.toArray()[0]).setType(Material.REDSTONE_TORCH_ON);
			placetorch = false;
		}
		
		open = true;
		FunnelBridgeManager.reorientBridge(this);
	}
	
	public void close()
	{
		byte color;
		if (orange)
			color = (byte) Util.getRightPortalColor(getOwner().getColorPreset());
		else
			color = (byte) Util.getLeftPortalColor(getOwner().getColorPreset());			
		for (Block b: inside)
    	{
    		b.setType(Material.WOOL);
    		b.setData(color);
    		open = false;
    	}
		
		FunnelBridgeManager.reorientBridge(this);
	}
	
	public void recreate()
	{
		byte color;
		if (orange)
			color = (byte) Util.getRightPortalColor(getOwner().getColorPreset());
		else
			color = (byte) Util.getLeftPortalColor(getOwner().getColorPreset());			
		
		for (Block b: border)
    	{
    		b.setData(color);
    	}

		if (!isOpen())
		{
			for (Block b: inside)
	    	{
	    		b.setData(color);
	    	}
		}
		
		if (Config.CompactPortal)
		{
			for (Block b: behind)
	    	{
	    		b.setData(color);
	    	}
		}
	}
	
	public void create()
	{
		byte color;
		if (orange)
			color = (byte) Util.getRightPortalColor(getOwner().getColorPreset());
		else
			color = (byte) Util.getLeftPortalColor(getOwner().getColorPreset());			

    	for (Block b: border)
    	{
    		if (PortalManager.borderBlocks.containsKey((b.getLocation())))
    			PortalManager.borderBlocks.get(b.getLocation()).delete();
    		if (PortalManager.insideBlocks.containsKey((b.getLocation())))
    			PortalManager.insideBlocks.get(b.getLocation()).delete();
    		if (PortalManager.behindBlocks.containsKey((b.getLocation())))
    			PortalManager.behindBlocks.get(b.getLocation()).delete();
    		
    		oldBlocks.put(b.getLocation(), BlockUtil.getBlockData(b));
    		b.setType(Material.WOOL);
    		b.setData(color);
    		PortalManager.borderBlocks.put(b.getLocation(), this);
       	}
    	for (Block b: inside)
    	{
			oldBlocks.put(b.getLocation(), BlockUtil.getBlockData(b));
    	}
    	if (Config.FillPortalBack > -1)
    	{
    		for (Block b: behind)
        	{
        		if (PortalManager.borderBlocks.containsKey((b.getLocation())))
        			PortalManager.borderBlocks.get(b.getLocation()).delete();
        		if (PortalManager.insideBlocks.containsKey((b.getLocation())))
        			PortalManager.insideBlocks.get(b.getLocation()).delete();
        		if (PortalManager.behindBlocks.containsKey((b.getLocation())))
        			PortalManager.behindBlocks.get(b.getLocation()).delete();

        		oldBlocks.put(b.getLocation(), BlockUtil.getBlockData(b));
        		if (Config.CompactPortal)
        		{
        			b.setType(Material.WOOL);
            		b.setData(color);
        		}
        		else
        		{
        			b.setTypeId(Config.FillPortalBack);
        		}
        		PortalManager.behindBlocks.put(b.getLocation(), this);
        	}
    	}
    	
    		if (getDestination() == null)
    			close();
    		else
    		{
    			open();
	    		if (getDestination().isRegionPortal())
	    		{
					RegionManager.getRegion(getCenterBlock().getLocation()).regionPortalOpened(orange);
	    		}
	    		else
	    		{
	    			getDestination().open();
	    		}

    		}
    		
    		if (isRegionPortal())
    		{
    			RegionManager.getRegion(centerBlock.getLocation()).regionPortalCreated(orange);
    		}
    			    	
    	for (Block b : inside)
    	{
    		PortalManager.insideBlocks.put(b.getLocation(), this);
    		
    		for (int x = -2;x<3;x++)
    		{
    			for (int y = -2;y<3;y++)
        		{
    				for (int z = -2;z<3;z++)
    	    		{
    	    			PortalManager.awayBlocksGeneral.put(b.getRelative(x,y,z).getLocation(), this);
    	    			awayBlocks.add(b.getRelative(x,y,z).getLocation());
    	    		}
        		}
    		}
    		
    			for (int y = -2;y<3;y++)
        		{
    				for (int z = -2;z<3;z++)
    	    		{
    	    			PortalManager.awayBlocksX.put(b.getRelative(3,y,z).getLocation(), this);
    	    			PortalManager.awayBlocksX.put(b.getRelative(-3,y,z).getLocation(), this);
    	    			awayBlocks.add(b.getRelative(3,y,z).getLocation());
    	    			awayBlocks.add(b.getRelative(-3,y,z).getLocation());
    	    		}
        		}
    			
    			for (int x = -2;x<3;x++)
        		{
    				for (int z = -2;z<3;z++)
    	    		{
    	    			PortalManager.awayBlocksY.put(b.getRelative(x,3,z).getLocation(), this);
    	    			PortalManager.awayBlocksY.put(b.getRelative(x,-3,z).getLocation(), this);
    	    			awayBlocks.add(b.getRelative(x,3,z).getLocation());
    	    			awayBlocks.add(b.getRelative(x,-3,z).getLocation());
    	    		}
        		}
    			
    			for (int x = -2;x<3;x++)
        		{
    				for (int y = -2;y<3;y++)
    	    		{
    	    			PortalManager.awayBlocksZ.put(b.getRelative(x,y,3).getLocation(), this);
    	    			PortalManager.awayBlocksZ.put(b.getRelative(x,y,-3).getLocation(), this);
    	    			awayBlocks.add(b.getRelative(x,y,3).getLocation());
    	    			awayBlocks.add(b.getRelative(x,y,-3).getLocation());
    	    		}
        		}
    		    		
    	}
	}
	
	public Location getTeleportLocation()
	{
		return teleport;
	}
	
	public User getOwner()
	{
		return owner;
	}
	
	public HashSet<Block> getBorder()
	{
		return border;
	}
	
	public HashSet<Block> getInside()
	{
		return inside;
	}
	
	public HashSet<Block> getBehind()
	{
		return behind;
	}
	
	public Boolean isOpen()
	{
		return open;
	}
	
	public Boolean isVertical()
	{
		return vertical;
	}
	
	public BlockFace getTeleportFace()
	{
		return teleportFace;
	}
	
	public Boolean isOrange()
	{
		return orange;
	}
	
	public Boolean isDisabled()
	{
		return disabled;
	}
	
	public void setDisabled(Boolean input)
	{
		disabled = input;
	}
	
	public Boolean isTransmitter()
	{
		return transmitter;
	}
	
	public void setTransmitter(Boolean input)
	{
		transmitter = input;
	}
	
	public void setPlaceTorch(Boolean input)
	{
		placetorch = true;
	}
	public Portal getDestination()
	{
		Region region = RegionManager.getRegion(centerBlock.getLocation());
		
		if (isOrange())
		{
			if (owner.getBluePortal() != null) 
				return owner.getBluePortal();
			else if (!isRegionPortal())
				return region.getBluePortal();
			else
				return region.bluePortalDest;
		}
		else
		{
			if (owner.getOrangePortal() != null) 
				return owner.getOrangePortal();
			else if (!isRegionPortal())
				return region.getOrangePortal();
			else
				return region.orangePortalDest;

		}
	}
	public Block getCenterBlock()
	{
		return centerBlock;
	}
	public Boolean isRegionPortal()
	{
		return owner.name.startsWith("region_");
	}
}
