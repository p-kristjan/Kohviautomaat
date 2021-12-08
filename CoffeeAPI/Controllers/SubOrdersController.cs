using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using CoffeeAPI.Data;
using CoffeeAPI.Models;
using Microsoft.AspNetCore.Authorization;

namespace CoffeeAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize]
    public class SubOrdersController : ControllerBase
    {
        private readonly ApiDbContext _context;

        public SubOrdersController(ApiDbContext context)
        {
            _context = context;
        }

        // GET: api/SubOrders
        [HttpGet]
        public async Task<ActionResult<IEnumerable<SubOrder>>> GetSubOrders()
        {
            return await _context.SubOrders.ToListAsync();
        }

        // GET: api/SubOrders/5
        [HttpGet("{id}")]
        public async Task<ActionResult<SubOrder>> GetSubOrder(int id)
        {
            var subOrder = await _context.SubOrders.FindAsync(id);

            if (subOrder == null)
            {
                return NotFound();
            }

            return subOrder;
        }

        // PUT: api/SubOrders/5
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPut("{id}")]
        public async Task<IActionResult> PutSubOrder(int id, SubOrder subOrder)
        {
            if (id != subOrder.Id)
            {
                return BadRequest();
            }

            _context.Entry(subOrder).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!SubOrderExists(id))
                {
                    return NotFound();
                }
                else
                {
                    throw;
                }
            }

            return NoContent();
        }

        // POST: api/SubOrders
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPost]
        public async Task<ActionResult<SubOrder>> PostSubOrder(SubOrder subOrder)
        {
            _context.SubOrders.Add(subOrder);
            await _context.SaveChangesAsync();

            return CreatedAtAction("GetSubOrder", new { id = subOrder.Id }, subOrder);
        }

        // DELETE: api/SubOrders/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteSubOrder(int id)
        {
            var subOrder = await _context.SubOrders.FindAsync(id);
            if (subOrder == null)
            {
                return NotFound();
            }

            _context.SubOrders.Remove(subOrder);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        private bool SubOrderExists(int id)
        {
            return _context.SubOrders.Any(e => e.Id == id);
        }
    }
}
