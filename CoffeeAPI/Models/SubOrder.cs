using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace CoffeeAPI.Models
{
    public class SubOrder
    {
        public int Id { get; set; }
        public int? OrderId { get; set; }
        public string DrinkName { get; set; }
        public int Amount { get; set; }
        public int DrinkSize { get; set; }
        public float Price { get; set; }
    }
}
